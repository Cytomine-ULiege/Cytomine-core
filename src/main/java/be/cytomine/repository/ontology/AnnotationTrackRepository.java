package be.cytomine.repository.ontology;

import be.cytomine.domain.image.SliceInstance;
import be.cytomine.domain.ontology.AnnotationTrack;
import be.cytomine.domain.ontology.Track;
import liquibase.repackaged.com.opencsv.bean.CsvToBean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface AnnotationTrackRepository extends JpaRepository<AnnotationTrack, Long>, JpaSpecificationExecutor<AnnotationTrack>  {

    Optional<AnnotationTrack> findByAnnotationIdentAndTrack(Long id, Track track);

    List<AnnotationTrack> findAllByTrack(Track track);

    List<AnnotationTrack> findAllBySlice(SliceInstance sliceInstance);

    List<AnnotationTrack> findAllByAnnotationIdent(Long id);

    List<AnnotationTrack> findBySliceAndTrack(SliceInstance slice, Track track);
}
