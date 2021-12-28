package be.cytomine.service.image;

import be.cytomine.domain.CytomineDomain;
import be.cytomine.domain.command.*;
import be.cytomine.domain.image.AbstractImage;
import be.cytomine.domain.image.CompanionFile;
import be.cytomine.domain.image.UploadedFile;
import be.cytomine.domain.security.SecUser;
import be.cytomine.exceptions.AlreadyExistException;
import be.cytomine.repository.image.CompanionFileRepository;
import be.cytomine.service.CurrentUserService;
import be.cytomine.service.ModelService;
import be.cytomine.service.security.SecurityACLService;
import be.cytomine.utils.CommandResponse;
import be.cytomine.utils.JsonObject;
import be.cytomine.utils.Task;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.springframework.security.acls.domain.BasePermission.READ;
import static org.springframework.security.acls.domain.BasePermission.WRITE;

@Slf4j
@Service
@Transactional
@AllArgsConstructor
public class CompanionFileService extends ModelService {

    private CurrentUserService currentUserService;

    private SecurityACLService securityACLService;

    private CompanionFileRepository companionFileRepository;


    @Override
    public Class currentDomain() {
        return CompanionFile.class;
    }

    @Override
    public CytomineDomain createFromJSON(JsonObject json) {
        return new CompanionFile().buildDomainFromJson(json, getEntityManager());
    }

    public Optional<CompanionFile> find(Long id) {
        Optional<CompanionFile> companionFile = companionFileRepository.findById(id);
        companionFile.ifPresent(cf -> {
            if (!securityACLService.hasRightToReadAbstractImageWithProject(cf.getImage())) {
                securityACLService.check(cf.container(),READ);
            }
        });
        return companionFile;
    }

    public CompanionFile get(Long id) {
        return find(id).orElse(null);
    }

    public List<CompanionFile> list(AbstractImage image) {
        if (!securityACLService.hasRightToReadAbstractImageWithProject(image)) {
            securityACLService.check(image.container(),READ);
        }
        return companionFileRepository.findAllByImage(image);
    }

    public List<CompanionFile> list(UploadedFile uploadedFile) {
        securityACLService.check(uploadedFile, READ);
        return companionFileRepository.findAllByUploadedFile(uploadedFile);
    }


    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @return Response structure (created domain data,..)
     */
    public CommandResponse add(JsonObject json) {
        SecUser currentUser = currentUserService.getCurrentUser();
        securityACLService.checkUser(currentUser);

        return executeCommand(new AddCommand(currentUser),null, json);

    }

    /**
     * Update this domain with new data from json
     * @param domain Domain to update
     * @param jsonNewData New domain datas
     * @return  Response structure (new domain data, old domain data..)
     */
    @Override
    public CommandResponse update(CytomineDomain domain, JsonObject jsonNewData, Transaction transaction) {
        SecUser currentUser = currentUserService.getCurrentUser();
        securityACLService.check(domain.container(),WRITE);
        return executeCommand(new EditCommand(currentUser, transaction), domain,jsonNewData);
    }

    /**
     * Delete this domain
     * @param domain Domain to delete
     * @param transaction Transaction link with this command
     * @param task Task for this command
     * @param printMessage Flag if client will print or not confirm message
     * @return Response structure (code, old domain,..)
     */
    @Override
    public CommandResponse delete(CytomineDomain domain, Transaction transaction, Task task, boolean printMessage) {
        SecUser currentUser = currentUserService.getCurrentUser();
        securityACLService.checkUser(currentUser);
        securityACLService.check(domain.container(),WRITE);

        Command c = new DeleteCommand(currentUser, transaction);
        return executeCommand(c,domain, null);
    }

    @Override
    public void checkDoNotAlreadyExist(CytomineDomain domain) {
        // TODO: with new session?
        Optional<CompanionFile> file = companionFileRepository.findByImageAndUploadedFile(((CompanionFile)domain).getImage(), ((CompanionFile)domain).getUploadedFile());
        if (file.isPresent() && !Objects.equals(file.get().getId(), domain.getId())) {
            throw new AlreadyExistException("Companion file already exists for AbstractImage " + ((CompanionFile)domain).getImage());
        }
    }


    @Override
    public List<Object> getStringParamsI18n(CytomineDomain domain) {
        return List.of(domain.getId(), ((CompanionFile)domain).getOriginalFilename());
    }

}
