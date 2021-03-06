package com.psl.cc.analytics.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.bouncycastle.util.encoders.Base64;
import org.json.JSONObject;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.psl.cc.analytics.util.AccountControllerUtil;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AccountControllerTest {

	private final String BASE_URL = "/api/v1";

	@Autowired
	private AccountControllerUtil utils;

	@Autowired
	private MockMvc mockMvc;

	@Test
	public void getAccountNames() throws Exception {
		utils.setup();
		String accessToken = getAccessToken("VivoSpAdmin", "password");
		mockMvc.perform(get(BASE_URL + "/accounts/name").header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk()).andExpect(jsonPath("$[0].accountId", is("100007512")))
				.andExpect(jsonPath("$[0].accountName", is("SIM Replacement Test")));
	}

	@Test
	public void getAccountNames_SysAdminRole() throws Exception {

		String accessToken = getAccessToken("Test1", "password");
		mockMvc.perform(get(BASE_URL + "/accounts/name").header("Authorization", "Bearer " + accessToken)
				.param("adminId", utils.getData().getString("adminId"))).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].accountId", is("100007512")))
				.andExpect(jsonPath("$[0].accountName", is("SIM Replacement Test")));
	}

	@Test
	public void getRatePlanCount() throws Exception {

		String accessToken = getAccessToken("VivoSpAdmin", "password");
		mockMvc.perform(get(BASE_URL + "/accounts/ratePlan").header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk()).andExpect(jsonPath("$[0].total", is(1)))
				.andExpect(jsonPath("$[0].ratePlan", is("Vivo Default RP")));

	}

	@Test
	public void getRatePlanCount_SysAdminRole() throws Exception {

		String accessToken = getAccessToken("Test1", "password");
		mockMvc.perform(get(BASE_URL + "/accounts/ratePlan").header("Authorization", "Bearer " + accessToken)
				.param("adminId", utils.getData().getString("adminId"))).andExpect(status().isOk())
				.andExpect(status().isOk()).andExpect(jsonPath("$[0].total", is(1)))
				.andExpect(jsonPath("$[0].ratePlan", is("Vivo Default RP")));
		utils.tearDown();
	}

	@Test
	public void getCommPlanCount() throws Exception {

		String accessToken = getAccessToken("VivoSpAdmin", "password");
		mockMvc.perform(get(BASE_URL + "/accounts/commPlan").header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk()).andExpect(jsonPath("$[0].total", is(1)))
				.andExpect(jsonPath("$[0].communicationPlan", is("Vivo Default CP")));
	}

	@Test
	public void getCommPlanCount_SysAdminRole() throws Exception {

		String accessToken = getAccessToken("Test1", "password");
		mockMvc.perform(get(BASE_URL + "/accounts/commPlan").header("Authorization", "Bearer " + accessToken)
				.param("adminId", utils.getData().getString("adminId"))).andExpect(status().isOk())
				.andExpect(status().isOk()).andExpect(jsonPath("$[0].total", is(1)))
				.andExpect(jsonPath("$[0].communicationPlan", is("Vivo Default CP")));
	}

	@Test
	public void getDeviceStatus() throws Exception {

		String accessToken = getAccessToken("VivoSpAdmin", "password");
		mockMvc.perform(get(BASE_URL + "/accounts/deviceStatus").header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk()).andExpect(jsonPath("$[0].status", is("INVENTORY")))
				.andExpect(jsonPath("$[0].total", is(1))).andExpect(jsonPath("$[1].status", is("REPLACED")))
				.andExpect(jsonPath("$[1].total", is(1)));
	}

	@Test
	public void getDeviceStatus_SysAdminRole() throws Exception {

		String accessToken = getAccessToken("Test1", "password");
		mockMvc.perform(get(BASE_URL + "/accounts/deviceStatus").header("Authorization", "Bearer " + accessToken)
				.param("adminId", utils.getData().getString("adminId"))).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].status", is("INVENTORY"))).andExpect(jsonPath("$[0].total", is(1)))
				.andExpect(jsonPath("$[1].status", is("REPLACED"))).andExpect(jsonPath("$[1].total", is(1)));
	}

	@Test
	public void getAccountNames_Error() throws Exception {

		String accessToken = getAccessToken("Test1", "password");
		mockMvc.perform(get(BASE_URL + "/accounts/name").header("Authorization", "Bearer " + accessToken)
				.param("accountId", "100007512")).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message", is("Admin Id is Mandetory for SYSADMIN Role")));
	}

	@Test
	public void getRatePlanCount_Error() throws Exception {

		String accessToken = getAccessToken("Test1", "password");
		mockMvc.perform(get(BASE_URL + "/accounts/ratePlan").header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message", is("Admin Id is Mandetory for SYSADMIN Role")));

	}

	@Test
	public void getCommPlanCount_Error() throws Exception {

		String accessToken = getAccessToken("Test1", "password");
		mockMvc.perform(get(BASE_URL + "/accounts/commPlan").header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message", is("Admin Id is Mandetory for SYSADMIN Role")));

	}

	@Test
	public void getDeviceStatus_Error() throws Exception {

		String accessToken = getAccessToken("Test1", "password");
		mockMvc.perform(get(BASE_URL + "/accounts/deviceStatus").header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message", is("Admin Id is Mandetory for SYSADMIN Role")));
	}

	private String getAccessToken(String username, String password) throws Exception {
		String authorization = "Basic " + new String(Base64.encode("ashish:secret".getBytes()));
		String contentType = MediaType.APPLICATION_JSON + ";charset=UTF-8";

		String content = mockMvc
				.perform(post("/oauth/token").header("Authorization", authorization)
						.contentType(MediaType.APPLICATION_FORM_URLENCODED).param("username", username)
						.param("password", password).param("grant_type", "password").param("scope", "read write"))
				.andExpect(status().isOk()).andExpect(content().contentType(contentType))
				.andExpect(jsonPath("$.access_token", is(notNullValue())))
				.andExpect(jsonPath("$.token_type", is(equalTo("bearer"))))
				.andExpect(jsonPath("$.refresh_token", is(notNullValue())))
				.andExpect(jsonPath("$.expires_in", is(greaterThan(4000))))
				.andExpect(jsonPath("$.scope", is(equalTo("read write")))).andReturn().getResponse()
				.getContentAsString();
		return new JSONObject(content).getString("access_token");
	}
}
