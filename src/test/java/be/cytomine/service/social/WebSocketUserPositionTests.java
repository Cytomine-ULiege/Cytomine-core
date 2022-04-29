package be.cytomine.service.social;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.exceptions.ServerException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(username = "superadmin")
@ExtendWith(MockitoExtension.class)
public class WebSocketUserPositionTests {

    @Autowired
    BasicInstanceBuilder builder;

    @Autowired
    WebSocketUserPositionHandler webSocketUserPositionHandler;

    @AfterEach
    public void resetSessions(){
        webSocketUserPositionHandler.sessions = new HashMap<>();
    }

    @Test
    public void create_session_for_not_connected_user() {
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getAttributes()).thenReturn(Map.of("userID", "54"));

        assertThat(webSocketUserPositionHandler.sessions.get("54")).isNull();
        webSocketUserPositionHandler.afterConnectionEstablished(session);
        assertThat(webSocketUserPositionHandler.sessions.get("54")).isNotEmpty();
    }

    @Test
    public void add_session_for_already_connected_user() {
        ConcurrentWebSocketSessionDecorator sessionDecorator = mock(ConcurrentWebSocketSessionDecorator.class);
        connectTwoSessions(sessionDecorator);

        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getAttributes()).thenReturn(Map.of("userID", "54"));

        webSocketUserPositionHandler.afterConnectionEstablished(session);
        assertThat(webSocketUserPositionHandler.sessions.get("54").length).isEqualTo(2);
        assertThat(webSocketUserPositionHandler.sessions.get("89").length).isEqualTo(1);
    }

    @Test
    public void add_track_session_to_not_tracked_user() {
        ConcurrentWebSocketSessionDecorator sessionDecorator = mock(ConcurrentWebSocketSessionDecorator.class);
        connectTwoSessions(sessionDecorator);

        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getAttributes()).thenReturn(Map.of("userID", "54"));

        assertThat(webSocketUserPositionHandler.sessionsTracked.get("89")).isNull();
        webSocketUserPositionHandler.handleMessage(session, new TextMessage("89"));
        assertThat(webSocketUserPositionHandler.sessionsTracked.get("89").length).isEqualTo(1);
    }

    @Test
    public void add_track_session_to_already_tracked_user() {
        ConcurrentWebSocketSessionDecorator sessionDecorator = mock(ConcurrentWebSocketSessionDecorator.class);
        connectTwoSessions(sessionDecorator);

        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getAttributes()).thenReturn(Map.of("userID", "54"));
        webSocketUserPositionHandler.sessionsTracked.put("89", new ConcurrentWebSocketSessionDecorator[]{sessionDecorator});

        assertThat(webSocketUserPositionHandler.sessionsTracked.get("89").length).isEqualTo(1);
        webSocketUserPositionHandler.handleMessage(session, new TextMessage("89"));
        assertThat(webSocketUserPositionHandler.sessionsTracked.get("89").length).isEqualTo(2);
    }

    @Test
    public void add_some_track_sessions_to_already_tracked_user() {
        ConcurrentWebSocketSessionDecorator sessionDecorator = mock(ConcurrentWebSocketSessionDecorator.class);
        connectTwoSessions(sessionDecorator);

        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getAttributes()).thenReturn(Map.of("userID", "54"));
        webSocketUserPositionHandler.sessionsTracked.put("89", new ConcurrentWebSocketSessionDecorator[]{sessionDecorator});

        // Simulate that user is connected to Cytomine with 3 browsers (3 sessions)
        ConcurrentWebSocketSessionDecorator[] sessionsDecorator = {sessionDecorator, sessionDecorator, sessionDecorator};
        WebSocketUserPositionHandler.sessions.put("54", sessionsDecorator);

        assertThat(webSocketUserPositionHandler.sessionsTracked.get("89").length).isEqualTo(1);
        webSocketUserPositionHandler.handleMessage(session, new TextMessage("89"));
        assertThat(webSocketUserPositionHandler.sessionsTracked.get("89").length).isEqualTo(4);
    }

    @Test
    public void send_position_to_connected_user() throws IOException {
        WebSocketSession session = mock(WebSocketSession.class);
        connectSession(session);

        when(session.isOpen()).thenReturn(true);
        doNothing().when(session).sendMessage(new TextMessage("position"));
        assertDoesNotThrow(() -> webSocketUserPositionHandler.userHasMoved("54", "position"));
    }

    @Test
    public void send_position_to_not_connected_user(){
        ServerException exception = assertThrows(ServerException.class, () -> webSocketUserPositionHandler.userHasMoved("54", "position"));
        assertThat(exception.getMessage()).isEqualTo("User id : 54 has any web socket session active");
    }

    @Test
    public void send_position_to_connected_user_session_failed() throws IOException {
        WebSocketSession session = mock(WebSocketSession.class);
        connectSession(session);

        when(session.isOpen()).thenReturn(true);
        when(session.getId()).thenReturn("1234");
        doThrow(IOException.class).when(session).sendMessage(new TextMessage("position"));
        ServerException exception = assertThrows(ServerException.class, () -> webSocketUserPositionHandler.userHasMoved("54", "position"));
        assertThat(exception.getMessage()).isEqualTo("Failed to send message to session : 1234");
    }

    private void connectSession(WebSocketSession session ){
        when(session.getAttributes()).thenReturn(Map.of("userID", "54"));
        webSocketUserPositionHandler.afterConnectionEstablished(session);
    }

    private void connectTwoSessions(ConcurrentWebSocketSessionDecorator sessionDecorator){
        ConcurrentWebSocketSessionDecorator[] sessionsDecorator = {sessionDecorator};
        WebSocketUserPositionHandler.sessions.put("54", sessionsDecorator);
        WebSocketUserPositionHandler.sessions.put("89", sessionsDecorator);
        assertThat(WebSocketUserPositionHandler.sessions.get("54").length).isEqualTo(1);
        assertThat(WebSocketUserPositionHandler.sessions.get("89").length).isEqualTo(1);
    }
}