package be.cytomine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewedAnnotationStatsEntry {
    private Long user;
    private Long all;
    private Long reviewed;
}
