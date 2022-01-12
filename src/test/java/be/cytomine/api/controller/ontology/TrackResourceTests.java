package be.cytomine.api.controller.ontology;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.domain.ontology.Track;
import be.cytomine.domain.project.Project;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(username = "superadmin")
public class TrackResourceTests {

    @Autowired
    private EntityManager em;

    @Autowired
    private BasicInstanceBuilder builder;

    @Autowired
    private MockMvc restTrackControllerMockMvc;

    @Test
    @Transactional
    public void list_tracks_by_imageinstance() throws Exception {
        Track track = builder.given_a_track();
        restTrackControllerMockMvc.perform(get("/api/imageinstance/{id}/track.json", track.getImage().getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.collection", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.collection[?(@.name=='"+track.getName()+"')]").exists());
    }

    @Test
    @Transactional
    public void list_tracks_by_imageinstance_not_exists() throws Exception {
        restTrackControllerMockMvc.perform(get("/api/imageinstance/{id}/track.json", 0))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void list_tracks_by_project() throws Exception {
        Track track = builder.given_a_track();
        restTrackControllerMockMvc.perform(get("/api/project/{id}/track.json", track.getProject().getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.collection", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.collection[?(@.name=='"+track.getName()+"')]").exists());
    }

    @Test
    @Transactional
    public void list_tracks_by_project_not_exists() throws Exception {
        restTrackControllerMockMvc.perform(get("/api/project/{id}/track.json", 0))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void get_a_track() throws Exception {
        Track track = builder.given_a_track();
        restTrackControllerMockMvc.perform(get("/api/track/{id}.json", track.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(track.getId().intValue()))
                .andExpect(jsonPath("$.class").value("be.cytomine.domain.ontology.Track"))
                .andExpect(jsonPath("$.color").value(track.getColor()))
                .andExpect(jsonPath("$.created").isNotEmpty())
        ;
    }

    @Test
    @Transactional
    public void get_an_unexisting_track() throws Exception {
        restTrackControllerMockMvc.perform(get("/api/track/{id}.json", 0))
                .andDo(print())
                .andExpect(status().isNotFound());
    }


    @Test
    @Transactional
    public void add_valid_track() throws Exception {
        Track track = builder.given_a_not_persisted_track();
        restTrackControllerMockMvc.perform(post("/api/track.json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(track.toJSON()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.printMessage").value(true))
                .andExpect(jsonPath("$.callback").exists())
                .andExpect(jsonPath("$.callback.trackID").exists())
                .andExpect(jsonPath("$.callback.method").value("be.cytomine.AddTrackCommand"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.command").exists())
                .andExpect(jsonPath("$.track.id").exists())
                .andExpect(jsonPath("$.track.name").value(track.getName()));
    }

    @Test
    @Transactional
    public void edit_valid_track() throws Exception {
        Track track = builder.given_a_track();
        restTrackControllerMockMvc.perform(put("/api/track/{id}.json", track.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(track.toJSON()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.printMessage").value(true))
                .andExpect(jsonPath("$.callback").exists())
                .andExpect(jsonPath("$.callback.trackID").exists())
                .andExpect(jsonPath("$.callback.method").value("be.cytomine.EditTrackCommand"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.command").exists())
                .andExpect(jsonPath("$.track.id").exists())
                .andExpect(jsonPath("$.track.name").value(track.getName()));

    }

    @Test
    @Transactional
    public void delete_track() throws Exception {
        Track track = builder.given_a_track();
        restTrackControllerMockMvc.perform(delete("/api/track/{id}.json", track.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(track.toJSON()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.printMessage").value(true))
                .andExpect(jsonPath("$.callback").exists())
                .andExpect(jsonPath("$.callback.trackID").exists())
                .andExpect(jsonPath("$.callback.method").value("be.cytomine.DeleteTrackCommand"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.command").exists())
                .andExpect(jsonPath("$.track.id").exists())
                .andExpect(jsonPath("$.track.name").value(track.getName()));

    }
}
