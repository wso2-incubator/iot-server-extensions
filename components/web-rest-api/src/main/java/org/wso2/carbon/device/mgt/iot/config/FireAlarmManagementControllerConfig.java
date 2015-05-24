/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.iot.config;

import org.wso2.carbon.device.mgt.iot.config.datasource.DataSourceConfigAdapter;
import org.wso2.carbon.device.mgt.iot.config.datasource.IotDataSourceConfig;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.List;
import java.util.Map;

/**
 * Class for holding device controller data.
 */
@XmlRootElement(name = "DeviceController")
public class FireAlarmManagementControllerConfig {

	private String deviceDataStore;
	private String deviceControlQueue;

	@XmlElement(name = "DeviceDataStore")
	public String getDeviceDataStore(){
		return deviceDataStore;
	}

	public void setDeviceDataStore(String deviceDataStore) {
		this.deviceDataStore = deviceDataStore;
	}

	@XmlElement(name = "DeviceControlQueue")
	public String getDeviceControlQueue() {
		return deviceControlQueue;
	}

	public void setDeviceControlQueue(String deviceControlQueue) {
		this.deviceControlQueue = deviceControlQueue;
	}

}
