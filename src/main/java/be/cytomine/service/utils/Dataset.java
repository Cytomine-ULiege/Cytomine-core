package be.cytomine.service.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Dataset {

    @Value("${application.serverURL}")
    public String CYTOMINEURL;

    public String ADMINLOGIN = "admin";

    @Value("${application.adminPassword}")
    public String ADMINPASSWORD;

    public String SUPERADMINLOGIN = "superadmin";

    @Value("${application.adminPassword}")
    public String SUPERADMINPASSWORD;

    public String ANOTHERLOGIN = "anotheruser";

    @Value("${application.adminPassword}")
    public String ANOTHERPASSWORD;

    @Value("${application.adminEmail}")
    public String ADMINEMAIL;

    public String GOODPASSWORDUSERJOB = "PasswordUserJob";

    public String BADLOGIN = "badlogin";
    public String BADPASSWORD = "badpassword";


    public String UNDOURL = "command/undo.json";
    public String REDOURL = "command/redo.json";
}
