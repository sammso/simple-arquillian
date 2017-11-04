/**
 * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
 * <p/>
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.arquillian.sample.service4injection;

import org.osgi.service.component.annotations.Component;

/**
 * @author Cristina González
 */
@Component(
	immediate = true,
	property = {
		"name=ServiceFirst", "service.ranking:Integer=" + Integer.MAX_VALUE
	},
	service = Service.class
)
public class ServiceFirstImpl implements Service {

	@Override
	public int operation(int a) {
		return a + 3;
	}

}