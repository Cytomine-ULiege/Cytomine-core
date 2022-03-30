package be.cytomine.service.utils;

import be.cytomine.domain.ontology.AnnotationDomain;
import be.cytomine.service.dto.Point;
import be.cytomine.service.image.ImageInstanceService;
import be.cytomine.service.ontology.ReviewedAnnotationService;
import be.cytomine.service.ontology.TermService;
import be.cytomine.service.ontology.UserAnnotationService;
import be.cytomine.service.report.ReportColumn;
import be.cytomine.service.security.SecUserService;
import be.cytomine.utils.StringUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class ReportFormatService {

    private final TermService termService;

    private final UserAnnotationService userAnnotationService;

    private final ReviewedAnnotationService reviewedAnnotationService;

    private Map<Long,String> termNameCache;

    /**
     * Transform a List<Map<String,Object>> of annotation into an Object[][]
     * with headers corresponding to given columns.
     *
     * @param  columns
     * @param  data
     * @return Object[][]
     */
    public Object[][] formatAnnotationsForReport(List<ReportColumn> columns, List<Map<String, Object>> data){
        Object[] headers = getColumnHeaders(columns);
        Object[][] report = initReport(data, headers);

        for(int i = 0; i < data.size(); i++){
            Map<String, Object> element = data.get(i);
            for(int j = 0; j < headers.length; j++){

                Object value = getAnnotationValue(element.get(headers[j]), element, headers[j].toString());

                if(value == null){
                    value = "";
                }
                report[i + 1][j] = value;
            }
        }
        headerPropertyToTitle(columns, report);
        return report;
    }

    /**
     * Transform a List<Map<String,Object>> of users into an Object[][]
     * with headers corresponding to given columns.
     *
     * @param  columns
     * @param  data
     * @return Object[][]
     */
    public Object[][] formatUsersForReport(List<ReportColumn> columns, List<Map<String, Object>> data){
        Object[] headers = getColumnHeaders(columns);
        Object[][] report = initReport(data, headers);

        for(int i = 0; i < data.size(); i++){
            Map<String, Object> element = data.get(i);
            for(int j = 0; j < headers.length; j++){

                Object value = element.get(headers[j]);

                if(value == null){
                    value = "";
                }
                report[i + 1][j] = value;
            }
        }
        headerPropertyToTitle(columns, report);
        return report;
    }

    /**
     * Get value for annotation report
     *
     * @param  value
     * @param  annotation
     * @param  header
     * @return String
     */
    private Object getAnnotationValue(Object value, Map<String, Object> annotation, String header){
        Point centroid = (Point) annotation.get("centroid");
        switch (header){
            case "user":
                return annotation.get("creator");
            case "filename":
                return annotation.get("instanceFilename");
            case "term":
                return String.join("- ", getTermsName(value));
            case "area" : case "perimeter":
                return StringUtils.decimalFormatter(value);
            case "X":
                return StringUtils.decimalFormatter(centroid.getX());
            case "Y":
                return StringUtils.decimalFormatter(centroid.getY());
            default:
                return value;
        }
    }

    /**
     * @param  value Object representing the list of term ids
     * @return String[] terms name
     */
    public String[] getTermsName(Object value){
        String[] termsId = value.toString()
                .replace("[", "")
                .replace("]", "")
                .split(",");

        String[] termNames = new String[termsId.length];
        if(!termsId[0].trim().isEmpty()){
            int k = 0;
            for (String termId:termsId) {
                termNames[k] = getTermName(Long.parseLong(termId.trim()));
                k++;
            }
        }
        if(termsId[0].trim().isEmpty()){
            return new String[] {""};
        }else{
            return termNames;
        }
    }

    /**
     * Get column width from a list of ReportColumn
     *
     * @param  columns list of ReportColumn
     * @return ReportColumn width
     */
    public float[] getColumnWidth(List<ReportColumn> columns){
        float[] columnWidth = new float[columns.size()];
        for(int i=0; i<columns.size(); i++){
            columnWidth[i] = columns.get(i).columnWidth;
        }
        return columnWidth;
    }

    /**
     * Get report column headers from a list of ReportColumn
     *
     * @param  columns list of ReportColumn
     * @return ReportColumn headers
     */
    public static Object[] getColumnHeaders(List<ReportColumn> columns){
        return columns.stream().map(reportColumn -> reportColumn.property).toArray();
    }

    /**
     * Convert data headers (actually property values of columns list)
     * to columns titles.
     *
     * @param  columns
     * @param  data
     * @return
     */
    private static void headerPropertyToTitle(List<ReportColumn> columns, Object[][] data){
        for(int i=0; i<data[0].length; i++){
            data[0][i] = columns.get(i).title;
        }
    }

    /**
     * Check if term is already in cache. If yes return term name, if not add it.
     * @param  termId
     * @return String term name
     */
    private String getTermName(Long termId) {
        if(termNameCache.containsKey(termId)){
            return termNameCache.get(termId);
        }else{
            String termName = termService.find(termId).get().getName();
            termNameCache.put(termId, termName);
            return termName;
        }
    }

    private Object[][] initReport(List<Map<String, Object>> data, Object[] headers){
        Object[][] report = new Object[data.size() + 1][headers.length];
        report[0] = headers;
        return report;
    }
}
