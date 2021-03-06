package com.psl.cc.analytics.scheduler;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.psl.cc.analytics.constants.ControlCentreConstants;
import com.psl.cc.analytics.util.SchedulerUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SchedulerTest {

	private final String accountResponse = "{\"pageNumber\":1,\"accounts\":[{\"accountName\":\"SIM Replacement Test\",\"accountId\":\"100007512\",\"type\":\"STANDARD\",\"status\":\"Active\",\"currency\":\"BRL\",\"operatorAccountId\":null,\"taxId\":\"-1\",\"commPlanDetails\":{\"defaultCommPlan\":\"Vivo Default CP\",\"defaultOnCommProfile\":\"VIVO_ON_CP\",\"defaultOffCommProfile\":\"Vivo_BRL_OFF_CP\",\"onCommProfileHlrTemplateId\":\"777777100003929\",\"offCommProfileHlrTemplateId\":\"777777100003930\"},\"defaultRatePlan\":{\"defaultRatePlanId\":20566,\"defaultRatePlanName\":\"Vivo Default RP\"}},"
			+ "{\"accountName\":\"Vivo Inventory\",\"accountId\":\"100002218\",\"type\":\"MASTER\",\"status\":\"Active\",\"currency\":\"BRL\",\"operatorAccountId\":\"0045678910\",\"taxId\":\"1234567\",\"commPlanDetails\":{\"defaultCommPlan\":\"Vivo Default CP\",\"defaultOnCommProfile\":\"VIVO_ON_CP\",\"defaultOffCommProfile\":\"Vivo_BRL_OFF_CP\",\"onCommProfileHlrTemplateId\":\"777777100003929\",\"offCommProfileHlrTemplateId\":\"777777100003930\"},\"defaultRatePlan\":{\"defaultRatePlanId\":20566,\"defaultRatePlanName\":\"Vivo Default RP\"}}],\"lastPage\":true}";
	private final String modifiedDevices = "{\"pageNumber\":1,\"devices\":[{\"iccid\":\"8901650600000009770\",\"status\":\"ONHOLD\",\"ratePlan\":\"Telkomsel Default RP\",\"communicationPlan\":\"Telkomsel POC Test\"},{\"iccid\":\"8962101012740000400\",\"status\":\"ONHOLD\",\"ratePlan\":\"Telkomsel Default RP\",\"communicationPlan\":\"5.52TelkomselAdapterTests\"}],\"lastPage\":true}";
	private final String modifiedDevices1 = "{\"pageNumber\":1,\"devices\":[],\"lastPage\":true}";
	private final String deviceDetails1 = "{\"iccid\":\"8901650600000009770\",\"imsi\":\"310650899990006\",\"msisdn\":\"882350899990006\",\"imei\":\"\",\"status\":\"ONHOLD\",\"ratePlan\":\"Telkomsel Default RP\",\"communicationPlan\":\"Telkomsel POC Test\",\"customer\":\"TestCust01\",\"endConsumerId\":null,\"dateActivated\":null,\"dateAdded\":\"2015-11-11 11:25:29.888+0000\",\"dateUpdated\":\"2016-05-11 05:31:29.089+0000\",\"dateShipped\":null,\"accountId\":\"100002064\",\"fixedIPAddress\":null,\"operatorCustom1\":\"\",\"operatorCustom2\":\"\",\"operatorCustom3\":\"\",\"operatorCustom4\":\"\",\"operatorCustom5\":\"\",\"accountCustom1\":\"\",\"accountCustom2\":\"\",\"accountCustom3\":\"\",\"accountCustom4\":\"\",\"accountCustom5\":\"\",\"accountCustom6\":\"\",\"accountCustom7\":\"\",\"accountCustom8\":\"\",\"accountCustom9\":\"\",\"accountCustom10\":\"\",\"customerCustom1\":\"\",\"customerCustom2\":\"\",\"customerCustom3\":\"\",\"customerCustom4\":\"\",\"customerCustom5\":\"\",\"simNotes\":\"Pre-transfer\",\"euiccid\":null,\"deviceID\":null,\"modemID\":null,\"globalSimType\":\"\"}";
	private final String deviceDetails2 = "{\"iccid\":\"8962101012740000400\",\"imsi\":\"510101274000021\",\"msisdn\":\"66868894499\",\"imei\":\"\",\"status\":\"ONHOLD\",\"ratePlan\":\"Telkomsel Default RP\",\"communicationPlan\":\"5.52TelkomselAdapterTests\",\"customer\":null,\"endConsumerId\":null,\"dateActivated\":null,\"dateAdded\":\"2015-11-13 07:52:36.885+0000\",\"dateUpdated\":\"2016-07-13 09:05:41.612+0000\",\"dateShipped\":null,\"accountId\":\"100002064\",\"fixedIPAddress\":null,\"operatorCustom1\":\"\",\"operatorCustom2\":\"\",\"operatorCustom3\":\"\",\"operatorCustom4\":\"\",\"operatorCustom5\":\"\",\"accountCustom1\":\"\",\"accountCustom2\":\"\",\"accountCustom3\":\"\",\"accountCustom4\":\"\",\"accountCustom5\":\"\",\"accountCustom6\":\"\",\"accountCustom7\":\"\",\"accountCustom8\":\"\",\"accountCustom9\":\"\",\"accountCustom10\":\"\",\"customerCustom1\":\"\",\"customerCustom2\":\"\",\"customerCustom3\":\"\",\"customerCustom4\":\"\",\"customerCustom5\":\"\",\"simNotes\":\"LBS test\",\"euiccid\":null,\"deviceID\":\"ChalitTelkSPA\",\"modemID\":\"ChalitTelkSPA\",\"globalSimType\":\"\"}";
	private final String baseUrl = "https://rws-jpotest.jasperwireless.com/rws";

	private String accountUri = null;
	private String modifiedDeviceUri = null;
	private String modifiedDeviceUri1 = null;
	private String deviceUri1 = null;
	private String deviceUri2 = null;

	@Autowired
	RestTemplate restTemplate;

	private MockRestServiceServer mockServer;

	@Autowired
	private SchedulerUtils util;

	@Autowired
	private GetAllAccounts accounts;

	@Value("${modifiedSinceYear}")
	private int modifiedSinceYear;

	final DateFormat dateFormat = new SimpleDateFormat(ControlCentreConstants.DATEFORMAT_DEVICESURL);

	@Before
	public void setup() throws Exception {
		mockServer = MockRestServiceServer.bindTo(restTemplate).ignoreExpectOrder(true).build();
		Calendar c = Calendar.getInstance();
		c.set(modifiedSinceYear, 0, 1, 0, 0, 0);
		String modifiedSince = dateFormat.format(c.getTime());

		accountUri = UriComponentsBuilder.fromUriString(baseUrl + ControlCentreConstants.ACCOUNTS_URL)
				.queryParam("pageNumber", String.valueOf(1)).build().toUri().toString();

		modifiedDeviceUri = UriComponentsBuilder.fromUriString(baseUrl + ControlCentreConstants.DEVICES_URL)
				.queryParam("pageNumber", String.valueOf(1))
				.queryParam("modifiedSince", URLEncoder.encode(modifiedSince, StandardCharsets.UTF_8.toString()))
				.queryParam("accountId", "100007512").queryParam("pageSize", ControlCentreConstants.PAGE_SIZE)
				.build(true).toUri().toString();

		modifiedDeviceUri1 = UriComponentsBuilder.fromUriString(baseUrl + ControlCentreConstants.DEVICES_URL)
				.queryParam("pageNumber", String.valueOf(1))
				.queryParam("modifiedSince", URLEncoder.encode(modifiedSince, StandardCharsets.UTF_8.toString()))
				.queryParam("accountId", "100002218").queryParam("pageSize", ControlCentreConstants.PAGE_SIZE)
				.build(true).toUri().toString();

		deviceUri1 = UriComponentsBuilder.fromUriString(baseUrl + ControlCentreConstants.DEVICES_URL)
				.path("/8901650600000009770").build(true).toUri().toString();
		deviceUri2 = UriComponentsBuilder.fromUriString(baseUrl + ControlCentreConstants.DEVICES_URL)
				.path("/8962101012740000400").build(true).toUri().toString();

		mockServer.expect(requestTo(accountUri)).andExpect(method(HttpMethod.GET))
				.andExpect(header(HttpHeaders.AUTHORIZATION, startsWith("Basic")))
				.andRespond(withStatus(HttpStatus.OK).body(accountResponse).contentType(MediaType.APPLICATION_JSON));

		mockServer.expect( requestTo(modifiedDeviceUri1)).andExpect(method(HttpMethod.GET))
				.andExpect(header(HttpHeaders.AUTHORIZATION, startsWith("Basic")))
				.andRespond(withStatus(HttpStatus.OK).body(modifiedDevices1).contentType(MediaType.APPLICATION_JSON));

		mockServer.expect( requestTo(modifiedDeviceUri)).andExpect(method(HttpMethod.GET))
				.andExpect(header(HttpHeaders.AUTHORIZATION, startsWith("Basic")))
				.andRespond(withStatus(HttpStatus.OK).body(modifiedDevices).contentType(MediaType.APPLICATION_JSON));

		mockServer.expect( requestTo(deviceUri1)).andExpect(method(HttpMethod.GET))
				.andExpect(header(HttpHeaders.AUTHORIZATION, startsWith("Basic")))
				.andRespond(withStatus(HttpStatus.OK).body(deviceDetails1).contentType(MediaType.APPLICATION_JSON));

		mockServer.expect( requestTo(deviceUri2)).andExpect(method(HttpMethod.GET))
				.andExpect(header(HttpHeaders.AUTHORIZATION, startsWith("Basic")))
				.andRespond(withStatus(HttpStatus.OK).body(deviceDetails2).contentType(MediaType.APPLICATION_JSON));

	}

	@Test
	public void a_InitializeFirstTime() throws Exception {
		util.setup();
		accounts.initializeFirstTime();

	}

	@Test
	public void cronJob() throws Exception {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.DATE, -1);
		c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE), 0, 0, 0);
		String modifiedSince = dateFormat.format(c.getTime());

		modifiedDeviceUri = UriComponentsBuilder.fromUriString(baseUrl + ControlCentreConstants.DEVICES_URL)
				.queryParam("pageNumber", String.valueOf(1))
				.queryParam("modifiedSince", URLEncoder.encode(modifiedSince, StandardCharsets.UTF_8.toString()))
				.queryParam("accountId", "100007512").queryParam("pageSize", ControlCentreConstants.PAGE_SIZE)
				.build(true).toUri().toString();

		modifiedDeviceUri1 = UriComponentsBuilder.fromUriString(baseUrl + ControlCentreConstants.DEVICES_URL)
				.queryParam("pageNumber", String.valueOf(1))
				.queryParam("modifiedSince", URLEncoder.encode(modifiedSince, StandardCharsets.UTF_8.toString()))
				.queryParam("accountId", "100002218").queryParam("pageSize", ControlCentreConstants.PAGE_SIZE)
				.build(true).toUri().toString();

		mockServer.expect( requestTo(modifiedDeviceUri1)).andExpect(method(HttpMethod.GET))
				.andExpect(header(HttpHeaders.AUTHORIZATION, startsWith("Basic")))
				.andRespond(withStatus(HttpStatus.OK).body(modifiedDevices1).contentType(MediaType.APPLICATION_JSON));

		mockServer.expect( requestTo(modifiedDeviceUri)).andExpect(method(HttpMethod.GET))
				.andExpect(header(HttpHeaders.AUTHORIZATION, startsWith("Basic")))
				.andRespond(withStatus(HttpStatus.OK).body(modifiedDevices).contentType(MediaType.APPLICATION_JSON));

		accounts.cronJob();

	}

	@Test
	public void cronJob_Accounts_Exception() throws Exception {
		mockServer = null;
		mockServer = MockRestServiceServer.bindTo(restTemplate).ignoreExpectOrder(true).build();
		accountUri = UriComponentsBuilder.fromUriString(baseUrl + ControlCentreConstants.ACCOUNTS_URL)
				.queryParam("pageNumber", String.valueOf(1)).build().toUri().toString();
		mockServer.expect(requestTo(accountUri)).andExpect(method(HttpMethod.GET))
				.andExpect(header(HttpHeaders.AUTHORIZATION, startsWith("Basic")))
				.andRespond(withStatus(HttpStatus.UNAUTHORIZED));

		accounts.cronJob();

	}

	@Test
	public void cronJob_ModifiedAccounts_Exception() throws Exception {
		mockServer = null;
		mockServer = MockRestServiceServer.bindTo(restTemplate).ignoreExpectOrder(true).build();
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.DATE, -1);
		c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE), 0, 0, 0);
		String modifiedSince = dateFormat.format(c.getTime());

		accountUri = UriComponentsBuilder.fromUriString(baseUrl + ControlCentreConstants.ACCOUNTS_URL)
				.queryParam("pageNumber", String.valueOf(1)).build().toUri().toString();

		mockServer.expect(requestTo(accountUri)).andExpect(method(HttpMethod.GET))
				.andExpect(header(HttpHeaders.AUTHORIZATION, startsWith("Basic")))
				.andRespond(withStatus(HttpStatus.OK).body(accountResponse).contentType(MediaType.APPLICATION_JSON));

		modifiedDeviceUri = UriComponentsBuilder.fromUriString(baseUrl + ControlCentreConstants.DEVICES_URL)
				.queryParam("pageNumber", String.valueOf(1))
				.queryParam("modifiedSince", URLEncoder.encode(modifiedSince, StandardCharsets.UTF_8.toString()))
				.queryParam("accountId", "100007512").queryParam("pageSize", ControlCentreConstants.PAGE_SIZE)
				.build(true).toUri().toString();

		modifiedDeviceUri1 = UriComponentsBuilder.fromUriString(baseUrl + ControlCentreConstants.DEVICES_URL)
				.queryParam("pageNumber", String.valueOf(1))
				.queryParam("modifiedSince", URLEncoder.encode(modifiedSince, StandardCharsets.UTF_8.toString()))
				.queryParam("accountId", "100002218").queryParam("pageSize", ControlCentreConstants.PAGE_SIZE)
				.build(true).toUri().toString();

		mockServer.expect( requestTo(modifiedDeviceUri1)).andExpect(method(HttpMethod.GET))
				.andExpect(header(HttpHeaders.AUTHORIZATION, startsWith("Basic")))
				.andRespond(withStatus(HttpStatus.OK).body(modifiedDevices1).contentType(MediaType.APPLICATION_JSON));

		mockServer.expect( requestTo(modifiedDeviceUri)).andExpect(method(HttpMethod.GET))
				.andExpect(header(HttpHeaders.AUTHORIZATION, startsWith("Basic")))
				.andRespond(withStatus(HttpStatus.UNAUTHORIZED));

		accounts.cronJob();
		util.tearDown();
	}

	@Test
	public void cronJob_Device_Exception() throws Exception {
		mockServer = null;
		mockServer = MockRestServiceServer.bindTo(restTemplate).ignoreExpectOrder(true).build();
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.DATE, -1);
		c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE), 0, 0, 0);
		String modifiedSince = dateFormat.format(c.getTime());

		accountUri = UriComponentsBuilder.fromUriString(baseUrl + ControlCentreConstants.ACCOUNTS_URL)
				.queryParam("pageNumber", String.valueOf(1)).build().toUri().toString();
		deviceUri1 = UriComponentsBuilder.fromUriString(baseUrl + ControlCentreConstants.DEVICES_URL)
				.path("/8901650600000009770").build(true).toUri().toString();
		deviceUri2 = UriComponentsBuilder.fromUriString(baseUrl + ControlCentreConstants.DEVICES_URL)
				.path("/8962101012740000400").build(true).toUri().toString();
		modifiedDeviceUri = UriComponentsBuilder.fromUriString(baseUrl + ControlCentreConstants.DEVICES_URL)
				.queryParam("pageNumber", String.valueOf(1))
				.queryParam("modifiedSince", URLEncoder.encode(modifiedSince, StandardCharsets.UTF_8.toString()))
				.queryParam("accountId", "100007512").queryParam("pageSize", ControlCentreConstants.PAGE_SIZE)
				.build(true).toUri().toString();

		modifiedDeviceUri1 = UriComponentsBuilder.fromUriString(baseUrl + ControlCentreConstants.DEVICES_URL)
				.queryParam("pageNumber", String.valueOf(1))
				.queryParam("modifiedSince", URLEncoder.encode(modifiedSince, StandardCharsets.UTF_8.toString()))
				.queryParam("accountId", "100002218").queryParam("pageSize", ControlCentreConstants.PAGE_SIZE)
				.build(true).toUri().toString();

		mockServer.expect( requestTo(modifiedDeviceUri1)).andExpect(method(HttpMethod.GET))
				.andExpect(header(HttpHeaders.AUTHORIZATION, startsWith("Basic")))
				.andRespond(withStatus(HttpStatus.OK).body(modifiedDevices1).contentType(MediaType.APPLICATION_JSON));

		mockServer.expect( requestTo(modifiedDeviceUri)).andExpect(method(HttpMethod.GET))
				.andExpect(header(HttpHeaders.AUTHORIZATION, startsWith("Basic")))
				.andRespond(withStatus(HttpStatus.OK).body(modifiedDevices).contentType(MediaType.APPLICATION_JSON));

		mockServer.expect( requestTo(deviceUri1)).andExpect(method(HttpMethod.GET))
				.andExpect(header(HttpHeaders.AUTHORIZATION, startsWith("Basic")))
				.andRespond(withStatus(HttpStatus.OK).body(deviceDetails1).contentType(MediaType.APPLICATION_JSON));

		mockServer.expect( requestTo(deviceUri2)).andExpect(method(HttpMethod.GET))
				.andExpect(header(HttpHeaders.AUTHORIZATION, startsWith("Basic")))
				.andRespond(withStatus(HttpStatus.UNAUTHORIZED));
		mockServer.expect(requestTo(accountUri)).andExpect(method(HttpMethod.GET))
				.andExpect(header(HttpHeaders.AUTHORIZATION, startsWith("Basic")))
				.andRespond(withStatus(HttpStatus.OK).body(accountResponse).contentType(MediaType.APPLICATION_JSON));

		accounts.cronJob();

	}

}
