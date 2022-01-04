package be.cytomine.service.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoundariesCropParameter {

    private Integer topLeftX;

    private Integer topLeftY;

    private Integer width;

    private Integer height;
}
