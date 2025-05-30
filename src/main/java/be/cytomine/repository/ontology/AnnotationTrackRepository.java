package be.cytomine.repository.ontology;

/*
* Copyright (c) 2009-2022. Authors: see NOTICE file.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import be.cytomine.domain.image.SliceInstance;
import be.cytomine.domain.ontology.AnnotationTrack;
import be.cytomine.domain.ontology.Track;
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
