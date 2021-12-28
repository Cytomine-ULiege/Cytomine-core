package be.cytomine.service.utils;

import be.cytomine.exceptions.WrongArgumentException;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.Tuple;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class ValidateGeometryService {

    EntityManager entityManager;

    // TODO: move this annotation service
    public String tryToMakeItValidIfNotValid(String location) {
        Geometry geometry = null;
        try {
            geometry = tryToMakeItValidIfNotValid(new WKTReader().read(location));
        } catch (Exception e) {
            return location;
        }
        return geometry.toText();
    }


    public Geometry tryToMakeItValidIfNotValid(Geometry location) {
        Geometry result = location;
        try {
            String backupLocation = location.toText();
            Geometry geom = new WKTReader().read(location.toText());
            Geometry validGeom;
            String type = geom.getGeometryType().toUpperCase();

            if (!geom.isValid()) {
                log.info("Geometry is not valid");
                //selfintersect,...
                validGeom = geom.buffer(0);
                result = validGeom;
                geom = new WKTReader().read(result.toText());
                type = geom.getGeometryType().toUpperCase();

                if (!geom.isValid() || geom.isEmpty()) {
                    //if not valid after buffer(0) or empty after buffer 0
                    //user_image already filter nested image

                    log.info("Geometry is not valid, even after a buffer(0)!");
                    String request = "SELECT ST_AsText(ST_MakeValid(ST_AsText('" + backupLocation + "')))";
                    log.info(request);
                    Query nativeQuery = entityManager.createNativeQuery("SELECT ST_AsText(ST_MakeValid(ST_AsText('" + backupLocation + "')))", Tuple.class);
                    List<Tuple> resultList = nativeQuery.getResultList();
                    for (Tuple tuple : resultList) {
                        String text = (String)tuple.get(0);
                        geom = new WKTReader().read(text);
                        type = geom.getGeometryType().toUpperCase();
                        if (type.equals("GEOMETRYCOLLECTION")) {
                            geom = geom.getGeometryN(0);
                            type = geom.getGeometryType().toUpperCase();
                        }
                        result = geom;
                    }

                }
            }

            if (geom.isEmpty()) {
                log.info("Geometry is empty");
                //empty polygon,...
                throw new WrongArgumentException(geom.toText() + " is an empty geometry!");
            }

            //for geometrycollection, we may take first collection element
            if (type.equals("MULTILINESTRING") || type.equals("GEOMETRYCOLLECTION")) {
                //geometry collection, take first elem
                throw new WrongArgumentException(geom.getGeometryType() + " is not a valid geometry type!");
            }
        } catch (ParseException exception) {

        }
        return result;
    }

}
