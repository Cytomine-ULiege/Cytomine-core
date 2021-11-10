package be.cytomine.repository.image;


import be.cytomine.domain.image.AbstractImage;
import be.cytomine.domain.image.ImageInstance;
import be.cytomine.domain.image.UploadedFile;
import be.cytomine.domain.project.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Spring Data JPA repository for the abstract image entity.
 */
@Repository
public interface ImageInstanceRepository extends JpaRepository<ImageInstance, Long>, JpaSpecificationExecutor<ImageInstance> {

    List<ImageInstance> findAllByProject(Project project);

    List<ImageInstance> findAllByBaseImage(AbstractImage abstractImage);

    boolean existsByBaseImage(AbstractImage abstractImage);

    default List<ImageInstance> findAllByBaseImage(AbstractImage abstractImage, Predicate<ImageInstance> predicate) {
        return findAllByBaseImage(abstractImage).stream().filter(predicate).collect(Collectors.toList());
    }

    @Query(value = "SELECT a.id FROM image_instance a WHERE project_id=:projectId AND parent_id IS NULL", nativeQuery = true)
    List<Long> getAllImageId(Long projectId);

    @Query(value = "SELECT a FROM image_instance a WHERE project_id=:projectId AND base_image_id=:baseImageId AND parent_id IS NULL", nativeQuery = true)
    Optional<ImageInstance> findByProjectIdAndBaseImageId(Long projectId, Long baseImageId);

    Optional<ImageInstance> findByProjectAndBaseImage(Project project, AbstractImage baseImage);

}
