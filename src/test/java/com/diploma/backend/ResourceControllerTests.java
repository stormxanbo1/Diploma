//// src/test/java/com/diploma/backend/ResourceControllerTests.java
//package com.diploma.backend;
//
//import com.diploma.backend.dto.CreateResourceRequest;
//import com.diploma.backend.entity.Role;
//import com.diploma.backend.util.SecurityHelpers;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.Set;
//import java.util.UUID;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//class ResourceControllerTests {
//
//    @Autowired MockMvc mockMvc;
//    @Autowired ObjectMapper om;
//    @Autowired SecurityHelpers helper;
//
//    String adminToken;
//    String staffToken;
//    String studentToken;
//    UUID groupId;
//    String studentEmail;
//
//    @BeforeEach
//    void setUp() throws Exception {
//        studentEmail = "u@" + SecurityHelpers.rnd();
//        adminToken   = helper.token("a@" + SecurityHelpers.rnd(), "p", Role.ADMIN);
//        staffToken   = helper.token("s@" + SecurityHelpers.rnd(), "p", Role.STAFF);
//        studentToken = helper.token(studentEmail,                     "p", Role.STUDENT);
//
//        // create a group under STAFF and add the student
//        groupId = helper.createTestGroupAndGetId(staffToken, studentEmail);
//    }
//
//    @Test @DisplayName("STUDENT cannot create resource → 403")
//    void studentCannotCreate() throws Exception {
//        CreateResourceRequest req = new CreateResourceRequest();
//        req.setName("R1"); req.setType("PDF"); req.setUrl("http://...");
//        req.setAllowedRoles(Set.of(Role.STAFF));
//        req.setAllowedGroupIds(Set.of(groupId));
//
//        mockMvc.perform(post("/api/resources")
//                        .header("Authorization", helper.bearer(studentToken))
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(om.writeValueAsString(req)))
//                .andDo(print())
//                .andExpect(status().isForbidden());
//    }
//
//    @Test @DisplayName("STAFF creates and STUDENT sees resource by group")
//    void staffCreateVisibleByStudentInGroup() throws Exception {
//        CreateResourceRequest req = new CreateResourceRequest();
//        req.setName("R2"); req.setType("LINK"); req.setUrl("http://link");
//        req.setAllowedGroupIds(Set.of(groupId));
//
//        String location = mockMvc.perform(post("/api/resources")
//                        .header("Authorization", helper.bearer(staffToken))
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(om.writeValueAsString(req)))
//                .andDo(print())
//                .andExpect(status().isCreated())
//                .andReturn().getResponse().getHeader("Location");
//
//        mockMvc.perform(get("/api/resources")
//                        .header("Authorization", helper.bearer(studentToken)))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[?(@.id=='" +
//                        location.substring(location.lastIndexOf('/') + 1) +
//                        "')]").exists());
//    }
//
//    @Test @DisplayName("STUDENT does not see unassigned resource")
//    void studentCannotSeeUnassigned() throws Exception {
//        CreateResourceRequest req = new CreateResourceRequest();
//        req.setName("R3"); req.setType("VIDEO"); req.setUrl("http://vid");
//
//        String loc = mockMvc.perform(post("/api/resources")
//                        .header("Authorization", helper.bearer(staffToken))
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(om.writeValueAsString(req)))
//                .andDo(print())
//                .andReturn().getResponse().getHeader("Location");
//
//        mockMvc.perform(get("/api/resources")
//                        .header("Authorization", helper.bearer(studentToken)))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[?(@.id=='" +
//                        loc.substring(loc.lastIndexOf('/') + 1) +
//                        "')]").doesNotExist());
//    }
//
//    @Test @DisplayName("ADMIN can delete resource → 204")
//    void adminCanDelete() throws Exception {
//        CreateResourceRequest req = new CreateResourceRequest();
//        req.setName("R4"); req.setType("PDF"); req.setUrl("u");
//
//        String loc = mockMvc.perform(post("/api/resources")
//                        .header("Authorization", helper.bearer(staffToken))
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(om.writeValueAsString(req)))
//                .andDo(print())
//                .andReturn().getResponse().getHeader("Location");
//
//        mockMvc.perform(delete(loc)
//                        .header("Authorization", helper.bearer(adminToken)))
//                .andDo(print())
//                .andExpect(status().isNoContent());
//    }
//}
