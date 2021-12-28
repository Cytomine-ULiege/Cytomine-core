package be.cytomine.domain.command;

import be.cytomine.domain.CytomineDomain;
import be.cytomine.domain.project.Project;
import be.cytomine.domain.security.SecUser;
import be.cytomine.service.ModelService;
import be.cytomine.utils.CommandResponse;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * @author Cytomine Team
 * The AddCommand class is a command that create a new domain
 * It provide an execute method that create domain from command, an undo method that drop domain and an redo method that recreate domain
 */
@Entity
@DiscriminatorValue("be.cytomine.command.AddCommand")
public class AddCommand extends Command {

    public AddCommand(SecUser currentUser) {
        this.user = currentUser;
    }

    public AddCommand(SecUser currentUser, Transaction transaction) {
        this.user = currentUser;
        this.transaction = transaction;
    }

    public AddCommand() {
        super();
    }

    /**
     * Process an Add operation for this command
     * @return Message
     */
    public CommandResponse execute(ModelService service) {
        //Create new domain from json data
        json.put("id", null);
        CytomineDomain newDomain = service.createFromJSON(json);
        //Save new domain in database
        CommandResponse response = service.create(newDomain, printMessage);
        //Init command domain
        newDomain = response.getObject();
        fillCommandInfo(newDomain, (String)response.getData().get("message"));
        if (newDomain != null) {
            CytomineDomain container = newDomain.container();
            if(container!=null && container instanceof Project) {
                super.setProject((Project)container);
            }
        }

        return response;
    }
}
