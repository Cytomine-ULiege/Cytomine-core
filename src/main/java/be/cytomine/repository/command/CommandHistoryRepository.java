package be.cytomine.repository.command;

import be.cytomine.domain.command.CommandHistory;
import be.cytomine.domain.project.Project;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommandHistoryRepository extends JpaRepository<CommandHistory, Long> {

    List<CommandHistory> findAllByProject(Project project, Pageable pageable);

    void deleteAllByProject(Project project);
}
