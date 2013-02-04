package be.cytomine.dependency

import be.cytomine.project.Project
import be.cytomine.test.Infos
import be.cytomine.test.http.OntologyAPI
import be.cytomine.test.http.TermAPI
import be.cytomine.utils.BasicInstance
import be.cytomine.ontology.*
import be.cytomine.test.http.UserAnnotationAPI

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
class UserAnnotationDependencyTests extends functionaltestplugin.FunctionalTestCase {


    void testUserAnnotationDependency() {

        def dependentDomain = createAnnotationWithDependency(BasicInstance.createBasicProjectNotExist())

        def annotation = dependentDomain.first()

        BasicInstance.checkIfDomainsExist(dependentDomain)

        //try to delete term
        assertEquals(200, UserAnnotationAPI.delete(annotation.id,Infos.GOODLOGIN,Infos.GOODPASSWORD).code)

        //check if all dependency are not aivalable
        BasicInstance.checkIfDomainsNotExist(dependentDomain)

        //undo op (re create)
        assertEquals(200, UserAnnotationAPI.undo(Infos.GOODLOGIN,Infos.GOODPASSWORD).code)


        //check if all dependency are aivalable
        BasicInstance.checkIfDomainsExist(dependentDomain)

        //redo op (re-delete)
        assertEquals(200, UserAnnotationAPI.redo(Infos.GOODLOGIN,Infos.GOODPASSWORD).code)

        //check if all dependency are not aivalable
        BasicInstance.checkIfDomainsNotExist(dependentDomain)
    }





    private def createAnnotationWithDependency(Project project) {
        //create a annotation and all its dependence domain

        UserAnnotation annotation = BasicInstance.createUserAnnotation(project)

        AnnotationTerm at =  BasicInstance.getBasicAnnotationTermNotExist("")
        at.userAnnotation = annotation
        BasicInstance.saveDomain(at)

        AlgoAnnotationTerm algoAnnotationTerm1 = BasicInstance.getBasicAlgoAnnotationTermNotExist()
        algoAnnotationTerm1.term = at.term
        algoAnnotationTerm1.expectedTerm = at.term
        algoAnnotationTerm1.annotation = annotation
        BasicInstance.saveDomain(algoAnnotationTerm1)

        return [annotation,at,algoAnnotationTerm1]
    }


}