package com.sohlman.liferay.arquillian.poc;

import com.liferay.arquillian.containter.remote.enricher.Inject;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.shrinkwrap.osgi.api.BndProjectBuilder;
import com.sohlman.liferay.arquillian.helper.CompanyTestHelper;
import com.sohlman.liferay.arquillian.helper.GroupTestHelper;
import com.sohlman.liferay.arquillian.helper.ServiceContextTestHelper;
import com.sohlman.liferay.arquillian.helper.UserTestHelper;

import java.io.File;
import java.util.List;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

//@RunWith(Arquillian.class)
public class UserLocalServiceTest {
	
	@Deployment
	public static JavaArchive create() {
		BndProjectBuilder bndProjectBuilder = ShrinkWrap.create(
			BndProjectBuilder.class);

		bndProjectBuilder.setBndFile(new File("bnd-basic-portlet-test.bnd"));

		bndProjectBuilder.generateManifest(true);

		return bndProjectBuilder.as(JavaArchive.class);
	}
	
	
	@Test
	public void testUserLocalService() throws Exception {
		List<User> list = UserLocalServiceUtil.getCompanyUsers(_companyTestHelper.getCompanyId(), QueryUtil.ALL_POS, QueryUtil.ALL_POS);
		
		for (User user : list) {
			_log.fatal(String.format("screenname: " + user.getScreenName()));
		}
		Assert.assertTrue(list.size() > 0);
		
		List<Role> roles = RoleLocalServiceUtil.getRoles(_companyTestHelper.getCompanyId());
		
		for (Role role : roles) {
			_log.fatal(String.format("role: " + role.getName()));
		}
		
		User user = _userTestHelper.getAdminUser(_companyTestHelper.getCompanyId());
	}
	
	@Inject
	private CompanyTestHelper _companyTestHelper;
	
	@Inject
	private UserTestHelper _userTestHelper;
	
	
	private Log _log = LogFactoryUtil.getLog("Arquillian");
}
