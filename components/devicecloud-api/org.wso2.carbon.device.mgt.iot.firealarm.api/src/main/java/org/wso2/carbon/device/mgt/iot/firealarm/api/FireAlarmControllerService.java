/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.device.mgt.iot.firealarm.api;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.iot.common.devicecloud.DeviceController;
import org.wso2.carbon.device.mgt.iot.common.devicecloud.exception.UnauthorizedException;
import org.wso2.carbon.device.mgt.iot.firealarm.api.util.DeviceJSON;
import org.wso2.carbon.device.mgt.iot.firealarm.constants.FireAlarmConstants;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

//import org.wso2.carbon.device.mgt.iot.firealarm.api.util.MQTTFirealarmSubscriber;

public class FireAlarmControllerService {

	private static Log log = LogFactory.getLog(FireAlarmControllerService.class);

	//    public static final String CONTROL_QUEUE_ENDPOINT;
	private static final String URL_PREFIX = "http://";
	private static final String BULB_CONTEXT = "/BULB/";
	private static final String FAN_CONTEXT = "/FAN/";
	private static final String TEMPERATURE_CONTEXT = "/TEMP/";

	private static CloseableHttpAsyncClient httpclient;
	private static Map<String, Map<String, String>> ownerTodevicesIPMap;
	//    private FireAlarmCallback fireAlarmCallback;

	//    private static Map<String, LinkedList<String>> replyMsgQueue;
	//    private static Map<String, LinkedList<String>> internalControlsQueue;
	//    private static MQTTFirealarmSubscriber mqttFireAlarmSubscriber;

	static {

        /*DeviceCloudManagementConfig config = null;
		try {
            config = DeviceCloudConfigManager.getInstance().getDeviceCloudMgtConfig();
        } catch (DeviceControllerException ex) {
            log.error("An error occured when trying to read device-cloud configurations", ex);
        }

        if (config != null) {
            // controller configurations
            DeviceCloudManagementControllerConfig controllerConfig = config
            .getDeviceCloudManagementControllerConfig();

            // reading control queue configurations
            String controlQueueKey = controllerConfig.getDeviceControlQueue();
            DeviceControlQueueConfig controlQueueConfig = config.getControlQueuesMap().get
            (controlQueueKey);
            if (controlQueueConfig == null) {
                log.error("Error occurred when trying to read control queue configurations");
            }

            String mqttUrl = "";
            String mqttPort = "";
            if (controlQueueConfig != null) {
                mqttUrl = controlQueueConfig.getEndPoint();
                mqttPort = controlQueueConfig.getPort();
            }

            CONTROL_QUEUE_ENDPOINT = mqttUrl + ":" + mqttPort;
            log.info("CONTROL_QUEUE_ENDPOINT Successfully initialized.");
        } else {
            CONTROL_QUEUE_ENDPOINT = null;
            log.error("CONTROL_QUEUE_ENDPOINT initialization failed.");
        }

        replyMsgQueue = new HashMap<>();
        internalControlsQueue = new HashMap<>();*/
		ownerTodevicesIPMap = new HashMap<>();

		httpclient = HttpAsyncClients.createDefault();
		httpclient.start();
	}

 /*   public void setMqttFireAlarmSubscriber(MQTTFirealarmSubscriber mqttFireAlarmSubscriber) {
		FireAlarmControllerService.mqttFireAlarmSubscriber = mqttFireAlarmSubscriber;
        try {
            mqttFireAlarmSubscriber.subscribe();
        } catch (DeviceManagementException ex) {
            log.error("Error occured when trying to subscribe the FireAlarmSubscriber", ex);
        }
    }

    public MQTTFirealarmSubscriber getMqttFireAlarmSubscriber() {
        return mqttFireAlarmSubscriber;
    }

    public static Map<String, LinkedList<String>> getReplyMsgQueue() {
        return replyMsgQueue;
    }

    public static Map<String, LinkedList<String>> getInternalControlsQueue() {
        return internalControlsQueue;
    }*/

	@Path("/register/{owner}/{deviceId}/{ip}")
	@GET
	public String registerDeviceIP(@PathParam("owner") String owner,
								   @PathParam("deviceId") String deviceId,
								   @PathParam("ip") String deviceIP,
								   @Context HttpServletResponse response) {
		String result;

		log.info("Got register call from IP: " + deviceIP + " for Device ID: " + deviceId +
						 " of owner: " + owner);
		Map<String, String> deviceIPMap = ownerTodevicesIPMap.get(owner);

		if (deviceIPMap == null) {
			deviceIPMap = new HashMap<>();
			deviceIPMap.put(deviceId, deviceIP);
			ownerTodevicesIPMap.put(owner, deviceIPMap);
		} else {
			deviceIPMap.put(deviceId, deviceIP);
		}

		result = "Device-IP Registered";
		response.setStatus(HttpStatus.SC_OK);

		if (log.isDebugEnabled()) {
			log.debug(result);
		}

		return result;
	}


    /*    Service to switch "ON" and "OFF" the FireAlarm bulb
			   Called by an external client intended to control the FireAlarm bulb */
/*    @Path("/bulb/{state}") @POST public void switchBulb(@HeaderParam("owner") String owner,
			@HeaderParam("deviceId") String deviceId, @PathParam("state") String state,
            @Context HttpServletResponse response) {

        String switchToState = state.toUpperCase();

        if (!switchToState.equals(FireAlarmConstants.STATE_ON) && !switchToState.equals
        (FireAlarmConstants.STATE_OFF)) {
            log.error("The requested state change shoud be either - 'ON' or 'OFF'");
            response.setStatus(HttpStatus.SC_BAD_REQUEST);
            return;
        }

        try {
            boolean result = DeviceController
                    .setControl(owner, FireAlarmConstants.DEVICE_TYPE, deviceId, "BULB",
                    switchToState);
            if (result) {
                response.setStatus(HttpStatus.SC_ACCEPTED);

            } else {
                response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            }

        } catch (UnauthorizedException e) {
            response.setStatus(HttpStatus.SC_UNAUTHORIZED);

        }
    }*/

	/*    Service to switch "ON" and "OFF" the FireAlarm bulb
		   Called by an external client intended to control the FireAlarm bulb */
	@Path("/bulb/{state}")
	@POST
	public void switchBulb(@HeaderParam("owner") String owner,
						   @HeaderParam("deviceId") String deviceId,
						   @PathParam("state") String state,
						   @Context HttpServletResponse response) {

		String switchToState = state.toUpperCase();

		if (!switchToState.equals(FireAlarmConstants.STATE_ON) && !switchToState.equals(
				FireAlarmConstants.STATE_OFF)) {
			log.error("The requested state change shoud be either - 'ON' or 'OFF'");
			response.setStatus(HttpStatus.SC_BAD_REQUEST);
			return;
		}

		Map<String, String> deviceIPMap = ownerTodevicesIPMap.get(owner);
		String deviceIP;

		if (deviceIPMap == null) {
			log.error("No live-registered devices exist for owner: " + owner);
			response.setStatus(HttpStatus.SC_NOT_FOUND);
			return;
		} else {
			deviceIP = deviceIPMap.get(deviceId);

			if (deviceIP == null) {
				log.error("IP not registered for device: " + deviceId + " of owner: " + owner);
				response.setStatus(HttpStatus.SC_PRECONDITION_FAILED);
				return;
			}
		}

		log.info("Sending command : " + switchToState + " to firealarm-BULB at : " + deviceIP);

		String callUrlPattern = BULB_CONTEXT + switchToState;
		try {
			sendCommand(deviceIP, 80, callUrlPattern);
		} catch (DeviceManagementException e) {
			response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			return;
		}
		response.setStatus(HttpStatus.SC_OK);
	}


    /*    Service to read the temperature from the FireAlarm temperature sensor
				   Called by an external client intended to get the current temperature */
/*    @Path("/readtemperature") @GET public String requestTemperature(@HeaderParam("owner")
String owner,
            @HeaderParam("deviceId") String deviceId, @Context HttpServletResponse response) {
        String replyMsg = "";

        try {
            boolean result = DeviceController
                    .setControl(owner, FireAlarmConstants.DEVICE_TYPE, deviceId, "TEMPERATURE",
                    "IN");
            if (result) {
                response.setStatus(HttpStatus.SC_ACCEPTED);
                replyMsg = "Request to read temperature sent to controls queue. Please wait...";
                return replyMsg;
            } else {
                response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                replyMsg = "An error occured whilst routing the request to queue. Re-try after
                some time...";
                return replyMsg;
            }

        } catch (UnauthorizedException e) {
            response.setStatus(HttpStatus.SC_UNAUTHORIZED);
            replyMsg = "An error occured whilst routing the request to queue. Re-try after some
            time...";
            return replyMsg;
        }
    }*/

	@Path("/readtemperature")
	@GET
	public String requestTemperature(@HeaderParam("owner") String owner,
									 @HeaderParam("deviceId") String deviceId,
									 @Context HttpServletResponse response) {
		String replyMsg = "";
		String deviceIP;

		Map<String, String> deviceIPMap = ownerTodevicesIPMap.get(owner);

		if (deviceIPMap == null) {
			replyMsg = "No live-registered devices exist for owner: " + owner;
			log.error(replyMsg);
			response.setStatus(HttpStatus.SC_NOT_FOUND);
			return replyMsg;
		} else {
			deviceIP = deviceIPMap.get(deviceId);

			if (deviceIP == null) {
				replyMsg = "IP not registered for device: " + deviceId + " of owner: " + owner;
				log.error(replyMsg);
				response.setStatus(HttpStatus.SC_PRECONDITION_FAILED);
				return replyMsg;
			}
		}

		log.info("Sending request to read firealarm-temperature at : " + deviceIP);

		try {
			replyMsg = sendCommand(deviceIP, 80, TEMPERATURE_CONTEXT);
		} catch (DeviceManagementException e) {
			replyMsg = e.getErrorMessage();
			response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			return replyMsg;
		}

		response.setStatus(HttpStatus.SC_OK);
		replyMsg = "The current temperature of the device is " + replyMsg;
		return replyMsg;
	}

    /*    Service to toggle the FireAlarm fan between "ON" and "OFF"
			   Called by an external client intended to control the FireAlarm fan */
/*    @Path("/fan/{state}") @POST public void switchFan(@HeaderParam("owner") String owner,
			@HeaderParam("deviceId") String deviceId, @PathParam("state") String state,
            @Context HttpServletResponse response) {

        String switchToState = state.toUpperCase();

        if (!switchToState.equals(FireAlarmConstants.STATE_ON) && !switchToState.equals
        (FireAlarmConstants.STATE_OFF)) {
            log.error("The requested state change shoud be either - 'ON' or 'OFF'");
            response.setStatus(HttpStatus.SC_BAD_REQUEST);
            return;
        }

        try {
            boolean result = DeviceController
                    .setControl(owner, FireAlarmConstants.DEVICE_TYPE, deviceId, "FAN",
                    switchToState);
            if (result) {
                response.setStatus(HttpStatus.SC_ACCEPTED);

            } else {
                response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);

            }

        } catch (UnauthorizedException e) {
            response.setStatus(HttpStatus.SC_UNAUTHORIZED);

        }
    }*/

	@Path("/fan/{state}")
	@POST
	public void switchFan(@HeaderParam("owner") String owner,
						  @HeaderParam("deviceId") String deviceId,
						  @PathParam("state") String state,
						  @Context HttpServletResponse response) {

		String switchToState = state.toUpperCase();

		if (!switchToState.equals(FireAlarmConstants.STATE_ON) && !switchToState.equals(
				FireAlarmConstants.STATE_OFF)) {
			log.error("The requested state change shoud be either - 'ON' or 'OFF'");
			response.setStatus(HttpStatus.SC_BAD_REQUEST);
			return;
		}

		Map<String, String> deviceIPMap = ownerTodevicesIPMap.get(owner);
		String deviceIP;

		if (deviceIPMap == null) {
			log.error("No live-registered devices exist for owner: " + owner);
			response.setStatus(HttpStatus.SC_NOT_FOUND);
			return;
		} else {
			deviceIP = deviceIPMap.get(deviceId);

			if (deviceIP == null) {
				log.error("IP not registered for device: " + deviceId + " of owner: " + owner);
				response.setStatus(HttpStatus.SC_PRECONDITION_FAILED);
				return;
			}
		}

		log.info("Sending command : " + switchToState + " to firealarm-FAN at : " + deviceIP);

		String callUrlPattern = FAN_CONTEXT + switchToState;
		try {
			sendCommand(deviceIP, 80, callUrlPattern);
		} catch (DeviceManagementException e) {
			response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			return;
		}

		response.setStatus(HttpStatus.SC_OK);
	}


    /*    Service to poll the control-queue for the controls sent to the FireAlarm
			   Called by the FireAlarm device  */
/*    @Path("/readcontrols/{owner}/{deviceId}") @GET public String readControls(@PathParam
("owner") String owner,
            @PathParam("deviceId") String deviceId, @Context HttpServletResponse response) {
        String result;
        LinkedList<String> deviceControlList = internalControlsQueue.get(deviceId);

        if (deviceControlList == null) {
            result = "No controls have been set for device " + deviceId + " of owner " + owner;
            response.setStatus(HttpStatus.SC_NO_CONTENT);
        } else {
            try {
                result = deviceControlList.remove(); //returns the  head value
                response.setStatus(HttpStatus.SC_ACCEPTED);

            } catch (NoSuchElementException ex) {
                result = "There are no more controls for device " + deviceId + " of owner " +
                        owner;
                response.setStatus(HttpStatus.SC_NO_CONTENT);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug(result);
        }

        return result;
    }*/

    /*    Service to send back the replies for the controls sent to the FireAlarm
		   Called by the FireAlarm device  */
	/*@Path("/reply") @POST @Consumes(MediaType.APPLICATION_JSON) public void reply(final
	DeviceJSON replyMsg,
            @Context HttpServletResponse response) {
        try {
            boolean result = DeviceController
                    .setControl(replyMsg.owner, FireAlarmConstants.DEVICE_TYPE, replyMsg
                    .deviceId, replyMsg.reply,
                            "OUT");
            if (result) {
                response.setStatus(HttpStatus.SC_ACCEPTED);

            } else {
                response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);

            }

        } catch (UnauthorizedException e) {
            response.setStatus(HttpStatus.SC_UNAUTHORIZED);

        }

    }*/

	/*    Service to push all the sensor data collected by the FireAlarm
		   Called by the FireAlarm device  */
	@Path("/pushalarmdata")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public void pushAlarmData(
			final DeviceJSON dataMsg, @Context HttpServletResponse response) {
		boolean result;
		String sensorValues = dataMsg.value;
		log.info("Recieved Sensor Data Values: " + sensorValues);

		String sensors[] = sensorValues.split(":");
		try {
			if (sensors.length == 3) {
				String temperature = sensors[0];
				String bulb = sensors[1];
				String fan = sensors[2];

				sensorValues = "Temperature:" + temperature + "C\tBulb Status:" + bulb +
						"\t\tFan Status:" +
						fan;
				log.info(sensorValues);

				result = DeviceController.pushData(dataMsg.owner, FireAlarmConstants.DEVICE_TYPE,
												   dataMsg.deviceId,
												   System.currentTimeMillis(), "DeviceData",
												   temperature, "TEMPERATURE");

				if (!result) {
					response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
					return;
				}

				result = DeviceController.pushData(dataMsg.owner, FireAlarmConstants.DEVICE_TYPE,
												   dataMsg.deviceId,
												   System.currentTimeMillis(), "DeviceData", bulb,
												   "BULB");

				if (!result) {
					response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
					return;
				}

				result = DeviceController.pushData(dataMsg.owner, FireAlarmConstants.DEVICE_TYPE,
												   dataMsg.deviceId,
												   System.currentTimeMillis(), "DeviceData", fan,
												   "FAN");

				if (!result) {
					response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
				}

			} else {
				result = DeviceController.pushData(dataMsg.owner, FireAlarmConstants.DEVICE_TYPE,
												   dataMsg.deviceId,
												   System.currentTimeMillis(), "DeviceData",
												   dataMsg.value, dataMsg.reply);
				if (!result) {
					response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
				}
			}

		} catch (UnauthorizedException e) {
			response.setStatus(HttpStatus.SC_UNAUTHORIZED);

		}

	}

	private String sendCommand(final String deviceIp, int deviceServerPort, String callUrlPattern)
			throws DeviceManagementException {

		if (deviceServerPort == 0) {
			deviceServerPort = 80;
		}

		String responseMsg = "";
		String urlString = URL_PREFIX + deviceIp + ":" + deviceServerPort + callUrlPattern;

		if (log.isDebugEnabled()) {
			log.debug(urlString);
		}

		if (callUrlPattern.contains(TEMPERATURE_CONTEXT)) {
			HttpURLConnection httpConnection = getHttpConnection(urlString);

			try {
				httpConnection.setRequestMethod(HttpMethod.GET);
			} catch (ProtocolException e) {
				String errorMsg =
						"Protocol specific error occurred when trying to set method to GET" +
								" for:" +
								urlString;
				log.error(errorMsg);
				throw new DeviceManagementException(errorMsg, e);
			}

			responseMsg = readResponseFromGetRequest(httpConnection);
			log.info(responseMsg.toString());

		} else {
			HttpGet request = new HttpGet(urlString);
			Future<HttpResponse> future;
			future = httpclient.execute(request, null);
		}

		return responseMsg;
	}

    /* Utility methods relevant to creating and sending http requests */

	/* This methods creates and returns a http connection object */
	private HttpURLConnection getHttpConnection(String urlString) throws
																  DeviceManagementException {

		URL connectionUrl = null;
		HttpURLConnection httpConnection = null;

		try {
			connectionUrl = new URL(urlString);
			httpConnection = (HttpURLConnection) connectionUrl.openConnection();
		} catch (MalformedURLException e) {
			String errorMsg =
					"Error occured whilst trying to form HTTP-URL from string: " + urlString;
			log.error(errorMsg);
			throw new DeviceManagementException(errorMsg, e);
		} catch (IOException e) {
			String errorMsg = "Error occured whilst trying to open a connection to: " +
					connectionUrl.toString();
			log.error(errorMsg);
			throw new DeviceManagementException(errorMsg, e);
		}

		return httpConnection;
	}

	/* This methods reads and returns the response from the connection */
	private String readResponseFromGetRequest(HttpURLConnection httpConnection)
			throws DeviceManagementException {
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(
					httpConnection.getInputStream()));
		} catch (IOException e) {
			String errorMsg =
					"There is an issue with connecting the reader to the input stream at: " +
							httpConnection.getURL();
			log.error(errorMsg);
			throw new DeviceManagementException(errorMsg, e);
		}

		String responseLine;
		StringBuffer completeResponse = new StringBuffer();

		try {
			while ((responseLine = bufferedReader.readLine()) != null) {
				completeResponse.append(responseLine);
			}
		} catch (IOException e) {
			String errorMsg =
					"Error occured whilst trying read from the connection stream at: " +
							httpConnection.getURL();
			log.error(errorMsg);
			throw new DeviceManagementException(errorMsg, e);
		}
		try {
			bufferedReader.close();
		} catch (IOException e) {
			log.error(
					"Could not succesfully close the bufferedReader to the connection at: " +
							httpConnection.getURL());
		}

		return completeResponse.toString();
	}

}
