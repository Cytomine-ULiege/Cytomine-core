package be.cytomine.exceptions;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * User: lrollus
 * Date: 17/11/11
 * This exception is the top exception for all cytomine exception
 * It store a message and a code, corresponding to an HTTP code
 */
public abstract class CytomineException extends RuntimeException {

    /**
     * Http code for an exception
     */
    public int code;

    /**
     * Message for exception
     */
    public String msg;

    /**
     * Values of the exception
     */
    public Map<Object, Object> values;

    /**
     * Message map with this exception
     * @param msg Message
     * @param code Http code
     */
    public CytomineException(String msg, int code) {
        this(msg,code,new HashMap<>());
    }
    public CytomineException(String msg, int code, Map<Object, Object> values) {
        this.msg=msg;
        this.code = code;
        this.values = values;
    }

    public String toString() {
        return this.msg;
    }
}
