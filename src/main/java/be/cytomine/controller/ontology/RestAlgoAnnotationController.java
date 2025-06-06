package be.cytomine.controller.ontology;

import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.io.ParseException;
import org.springframework.cloud.gateway.mvc.ProxyExchange;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import be.cytomine.controller.RestCytomineController;
import be.cytomine.domain.ontology.AlgoAnnotation;
import be.cytomine.domain.project.Project;
import be.cytomine.dto.image.CropParameter;
import be.cytomine.dto.json.JsonInput;
import be.cytomine.dto.json.JsonMultipleObject;
import be.cytomine.dto.json.JsonSingleObject;
import be.cytomine.exceptions.ObjectNotFoundException;
import be.cytomine.exceptions.WrongArgumentException;
import be.cytomine.service.ModelService;
import be.cytomine.service.middleware.ImageServerService;
import be.cytomine.service.ontology.AlgoAnnotationService;
import be.cytomine.service.ontology.SharedAnnotationService;
import be.cytomine.service.ontology.TermService;
import be.cytomine.service.project.ProjectService;
import be.cytomine.service.report.ReportService;
import be.cytomine.service.security.SecUserService;
import be.cytomine.service.utils.ParamsService;
import be.cytomine.utils.AnnotationListingBuilder;
import be.cytomine.utils.CommandResponse;
import be.cytomine.utils.JsonObject;
import be.cytomine.utils.RequestParams;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class RestAlgoAnnotationController extends RestCytomineController {

    private final AlgoAnnotationService algoAnnotationService;

    private final ProjectService projectService;

    private final AnnotationListingBuilder annotationListingBuilder;

    private final ImageServerService imageServerService;

    private final ParamsService paramsService;

    private final SharedAnnotationService sharedAnnotationService;

    private final ReportService reportService;

    private final SecUserService secUserService;

    private final TermService termService;

    /**
     * List all annotation (created by algo) visible for the current user
     */
    @GetMapping("/algoannotation.json")
    public ResponseEntity<String> list(
    ) throws IOException {
        log.debug("REST request to list algo annotation");
        List result = new ArrayList<>();
        List<String> propertyGroupToShow = paramsService.getPropertyGroupToShow(mergeQueryParamsAndBodyParams());
        for (Project project : projectService.listForCurrentUser()) {
            result.addAll(algoAnnotationService.list(project, propertyGroupToShow));
        }
        return responseSuccess(result);
    }

    @GetMapping("/algoannotation/{id}.json")
    public ResponseEntity<String> show(
            @PathVariable Long id
    ) {
        log.debug("REST request to get algo annotation : {}", id);
        return algoAnnotationService.find(id)
                .map(this::responseSuccess)
                .orElseGet(() -> responseNotFound("AlgoAnnotation", id));
    }

    @GetMapping("/project/{idProject}/algoannotation/count.json")
    public ResponseEntity<String> countByProject(
            @PathVariable(value = "idProject") Long idProject,
            @RequestParam(value="startDate", required = false) Long startDate,
            @RequestParam(value="endDate", required = false) Long endDate
    ) {
        log.debug("REST request to count algo annotation by project");
        Project project= projectService.find(idProject)
                .orElseThrow(() -> new ObjectNotFoundException("Project", idProject));
        Date start = (startDate!=null? new Date(startDate) : null);
        Date end = (endDate!=null? new Date(endDate) : null);
        return responseSuccess(JsonObject.of("total", algoAnnotationService.countByProject(project, start, end)));
    }

    @PostMapping("/algoannotation.json")
    public ResponseEntity<String> add(
            @RequestBody String json,
            @RequestParam(required = false) Long minPoint,
            @RequestParam(required = false) Long maxPoint
    ) {
        log.debug("REST request to save algo annotation");
        JsonInput data;
        try {
            data = new ObjectMapper().readValue(json, JsonMultipleObject.class);
            for (JsonObject datum : ((JsonMultipleObject) data)) {
                datum.putIfAbsent("minPoint", minPoint);
                datum.putIfAbsent("maxPoint", maxPoint);
            }
            // If fails to parse as a single object, parse as a list
        } catch (Exception ex) {
            try {
                data = new ObjectMapper().readValue(json, JsonSingleObject.class);
                ((JsonSingleObject)data).putIfAbsent("minPoint", minPoint);
                ((JsonSingleObject)data).putIfAbsent("maxPoint", maxPoint);
            } catch (JsonProcessingException e) {
                throw new WrongArgumentException("Json not valid");
            }
        }
        return add(algoAnnotationService, json);
    }

    public CommandResponse addOne(ModelService service, JsonObject json) {
        if (json.isMissing("location")) {
            throw new WrongArgumentException("Annotation must have a valid geometry:" + json.get("location"));
        }
        RequestParams requestParam = retrieveRequestParam();
        json.putIfAbsent("minPoint", requestParam.get("minPoint"));
        json.putIfAbsent("maxPoint", requestParam.get("maxPoint"));
        return service.add(json);
    }

    @PutMapping("/algoannotation/{id}.json")
    public ResponseEntity<String> edit(@PathVariable String id, @RequestBody JsonObject json) {
        log.debug("REST request to edit algo annotation : " + id);
        //get annotation from DB
        AlgoAnnotation domain = (AlgoAnnotation)algoAnnotationService.retrieve(json);
        //update it thanks to JSON in request
        CommandResponse result = algoAnnotationService.update(domain,json);
        return responseSuccess(result);
    }

    @DeleteMapping("/algoannotation/{id}.json")
    public ResponseEntity<String> delete(@PathVariable String id) {
        log.debug("REST request to delete an annotation : " + id);
        return delete(algoAnnotationService, JsonObject.of("id", id), null);
    }

    /**
     * DDownload a report (pdf, xls,...) with software annotation data from a specific project
     */
    @GetMapping("/project/{idProject}/algoannotation/download")
    public void downloadDocumentByProject(
            @PathVariable Long idProject,
            @RequestParam String format,
            @RequestParam String terms,
            @RequestParam String users,
            @RequestParam String images,
            @RequestParam(required = false) Long beforeThan,
            @RequestParam(required = false) Long afterThan
    ) throws IOException {
        Project project = projectService.find(idProject)
                .orElseThrow(() -> new ObjectNotFoundException("Project", idProject));
        users = secUserService.fillEmptyUserIds(users, idProject);
        terms = termService.fillEmptyTermIds(terms, project);
        JsonObject params = mergeQueryParamsAndBodyParams();
        byte[] report = annotationListingBuilder.buildAnnotationReport(idProject, users, params, terms, format);
        responseReportFile(reportService.getAnnotationReportFileName(format, idProject), report, format);
    }

    @RequestMapping(value = "/algoannotation/{id}/crop.{format}", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<byte[]> crop(
            @PathVariable Long id,
            @PathVariable String format,
            @RequestParam(required = false) Integer maxSize,
            @RequestParam(required = false) String geometry,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String boundaries,
            @RequestParam(defaultValue = "false") Boolean complete,
            @RequestParam(required = false) Integer zoom,
            @RequestParam(required = false) Double increaseArea,
            @RequestParam(required = false) Boolean safe,
            @RequestParam(required = false) Boolean square,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean draw,
            @RequestParam(required = false) Boolean mask,
            @RequestParam(required = false) Boolean alphaMask,
            @RequestParam(required = false) Boolean drawScaleBar,
            @RequestParam(required = false) Double resolution,
            @RequestParam(required = false) Double magnification,
            @RequestParam(required = false) String colormap,
            @RequestParam(required = false) Boolean inverse,
            @RequestParam(required = false) Double contrast,
            @RequestParam(required = false) Double gamma,
            @RequestParam(required = false) String bits,
            @RequestParam(required = false) Integer alpha,
            @RequestParam(required = false) Integer thickness,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) Integer jpegQuality,

            ProxyExchange<byte[]> proxy
    ) throws IOException, ParseException {
        log.debug("REST request to get associated image of a abstract image");
        AlgoAnnotation algoAnnotation = algoAnnotationService.find(id)
                .orElseThrow(() -> new ObjectNotFoundException("AlgoAnnotation", id));

        CropParameter cropParameter = new CropParameter();
        cropParameter.setGeometry(geometry);
        cropParameter.setLocation(location);
        cropParameter.setComplete(complete);
        cropParameter.setZoom(zoom);
        cropParameter.setMaxSize(maxSize);
        cropParameter.setIncreaseArea(increaseArea);
        cropParameter.setSafe(safe);
        cropParameter.setSquare(square);
        cropParameter.setType(type);
        cropParameter.setMaxSize(maxSize);
        cropParameter.setDraw(draw);
        cropParameter.setMask(mask);
        cropParameter.setAlphaMask(alphaMask);
        cropParameter.setDrawScaleBar(drawScaleBar);
        cropParameter.setResolution(resolution);
        cropParameter.setMagnification(magnification);
        cropParameter.setColormap(colormap);
        cropParameter.setInverse(inverse);
        cropParameter.setGamma(gamma);
        cropParameter.setAlpha(alpha);
        cropParameter.setContrast(contrast);
        cropParameter.setThickness(thickness);
        cropParameter.setColor(color);
        cropParameter.setJpegQuality(jpegQuality);
        cropParameter.setMaxBits(bits!=null && bits.equals("max"));
        cropParameter.setBits(bits!=null && !bits.equals("max") ? Integer.parseInt(bits): null);
        cropParameter.setFormat(format);

        String etag = getRequestETag();
        return  imageServerService.crop(algoAnnotation, cropParameter, etag, proxy);
    }

    @RequestMapping(value = "/algoannotation/{id}/mask.{format}", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<byte[]> cropMask(
            @PathVariable Long id,
            @PathVariable String format,
            @RequestParam(required = false) Integer maxSize,
            @RequestParam(required = false) String geometry,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String boundaries,
            @RequestParam(defaultValue = "false") Boolean complete,
            @RequestParam(required = false) Integer zoom,
            @RequestParam(required = false) Double increaseArea,
            @RequestParam(required = false) Boolean safe,
            @RequestParam(required = false) Boolean square,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean draw,
            @RequestParam(required = false) Boolean drawScaleBar,
            @RequestParam(required = false) Double resolution,
            @RequestParam(required = false) Double magnification,
            @RequestParam(required = false) String colormap,
            @RequestParam(required = false) Boolean inverse,
            @RequestParam(required = false) Double contrast,
            @RequestParam(required = false) Double gamma,
            @RequestParam(required = false) String bits,
            @RequestParam(required = false) Integer alpha,
            @RequestParam(required = false) Integer thickness,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) Integer jpegQuality,

            ProxyExchange<byte[]> proxy
    ) throws IOException, ParseException {
        log.debug("REST request to get associated image of a abstract image");
        AlgoAnnotation algoAnnotation = algoAnnotationService.find(id)
                .orElseThrow(() -> new ObjectNotFoundException("AlgoAnnotation", id));

        CropParameter cropParameter = new CropParameter();
        cropParameter.setGeometry(geometry);
        cropParameter.setLocation(location);
        cropParameter.setComplete(complete);
        cropParameter.setZoom(zoom);
        cropParameter.setMaxSize(maxSize);
        cropParameter.setIncreaseArea(increaseArea);
        cropParameter.setSafe(safe);
        cropParameter.setSquare(square);
        cropParameter.setType(type);
        cropParameter.setDraw(draw);
        cropParameter.setMask(true);
        cropParameter.setAlphaMask(false);
        cropParameter.setDrawScaleBar(drawScaleBar);
        cropParameter.setResolution(resolution);
        cropParameter.setMagnification(magnification);
        cropParameter.setColormap(colormap);
        cropParameter.setInverse(inverse);
        cropParameter.setGamma(gamma);
        cropParameter.setAlpha(alpha);
        cropParameter.setContrast(contrast);
        cropParameter.setThickness(thickness);
        cropParameter.setColor(color);
        cropParameter.setJpegQuality(jpegQuality);
        cropParameter.setMaxBits(bits!=null && bits.equals("max"));
        cropParameter.setBits(bits!=null && !bits.equals("max") ? Integer.parseInt(bits): null);
        cropParameter.setFormat(format);
        String etag = getRequestETag();
        return imageServerService.crop(algoAnnotation, cropParameter, etag, proxy);
    }

    @RequestMapping(value = "/algoannotation/{id}/alphamask.{format}", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<byte[]> cropAlphaMask(
            @PathVariable Long id,
            @PathVariable String format,
            @RequestParam(required = false) Integer maxSize,
            @RequestParam(required = false) String geometry,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String boundaries,
            @RequestParam(defaultValue = "false") Boolean complete,
            @RequestParam(required = false) Integer zoom,
            @RequestParam(required = false) Double increaseArea,
            @RequestParam(required = false) Boolean safe,
            @RequestParam(required = false) Boolean square,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean draw,
            @RequestParam(required = false) Boolean drawScaleBar,
            @RequestParam(required = false) Double resolution,
            @RequestParam(required = false) Double magnification,
            @RequestParam(required = false) String colormap,
            @RequestParam(required = false) Boolean inverse,
            @RequestParam(required = false) Double contrast,
            @RequestParam(required = false) Double gamma,
            @RequestParam(required = false) String bits,
            @RequestParam(required = false) Integer alpha,
            @RequestParam(required = false) Integer thickness,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) Integer jpegQuality,

            ProxyExchange<byte[]> proxy
    ) throws IOException, ParseException {
        log.debug("REST request to get associated image of a abstract image");
        AlgoAnnotation algoAnnotation = algoAnnotationService.find(id)
                .orElseThrow(() -> new ObjectNotFoundException("AlgoAnnotation", id));

        CropParameter cropParameter = new CropParameter();
        cropParameter.setGeometry(geometry);
        cropParameter.setLocation(location);
        cropParameter.setComplete(complete);
        cropParameter.setMaxSize(maxSize);
        cropParameter.setZoom(zoom);
        cropParameter.setIncreaseArea(increaseArea);
        cropParameter.setSafe(safe);
        cropParameter.setSquare(square);
        cropParameter.setType(type);
        cropParameter.setDraw(draw);
        cropParameter.setAlphaMask(true);
        cropParameter.setDrawScaleBar(drawScaleBar);
        cropParameter.setResolution(resolution);
        cropParameter.setMagnification(magnification);
        cropParameter.setColormap(colormap);
        cropParameter.setInverse(inverse);
        cropParameter.setGamma(gamma);
        cropParameter.setAlpha(alpha);
        cropParameter.setContrast(contrast);
        cropParameter.setThickness(thickness);
        cropParameter.setColor(color);
        cropParameter.setJpegQuality(jpegQuality);
        cropParameter.setMaxBits(bits!=null && bits.equals("max"));
        cropParameter.setBits(bits!=null && !bits.equals("max") ? Integer.parseInt(bits): null);
        cropParameter.setFormat(format);
        String etag = getRequestETag();
        return imageServerService.crop(algoAnnotation, cropParameter, etag, proxy);
    }

    /**
     * Add comment on an annotation to other user
     */
    @PostMapping("/algoannotation/{annotation}/comment.json")
    public ResponseEntity<String> addComment(
            @PathVariable(value = "annotation") Long annotationId,
            @RequestBody JsonObject json
    ) {
        log.debug("REST request to create comment for annotation : " + json);
        AlgoAnnotation annotation = algoAnnotationService.find(annotationId)
                .orElseThrow(()-> new ObjectNotFoundException("Annotation", annotationId));
        json.put("annotationIdent", annotation.getId());
        json.put("annotationClassName", annotation.getClass().getName());
        return responseSuccess(sharedAnnotationService.add(json));
    }

    /**
     * Show a single comment for an annotation
     */
    @GetMapping("/algoannotation/{annotation}/comment/{id}.json")
    public ResponseEntity<String> showComment(
            @PathVariable(value = "annotation") Long annotationId,
            @PathVariable(value = "id") Long commentId
    ) {
        log.debug("REST request to read comment {} for annotation {}", commentId, annotationId);
        AlgoAnnotation annotation = algoAnnotationService.find(annotationId)
                .orElseThrow(()-> new ObjectNotFoundException("Annotation", annotationId));
        return responseSuccess(sharedAnnotationService.find(commentId).orElseThrow(() ->
                new ObjectNotFoundException("SharedAnnotation", commentId)));
    }

    /**
     * List all comments for an annotation
     */
    @GetMapping("/algoannotation/{annotation}/comment.json")
    public ResponseEntity<String> listComments(
            @PathVariable(value = "annotation") Long annotationId
    ) {
        log.debug("REST request to read comments for annotation {}", annotationId);
        AlgoAnnotation annotation = algoAnnotationService.find(annotationId)
                .orElseThrow(()-> new ObjectNotFoundException("Annotation", annotationId));
        return responseSuccess(sharedAnnotationService.listComments(annotation));
    }
}
