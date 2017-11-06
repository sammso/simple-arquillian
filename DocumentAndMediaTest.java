package com.sohlman.liferay.arquillian.poc;

import com.liferay.arquillian.containter.remote.enricher.Inject;
import com.liferay.document.library.kernel.exception.NoSuchFolderException;
import com.liferay.document.library.kernel.model.DLFolderConstants;
import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.kernel.security.auth.PrincipalThreadLocal;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.MimeTypesUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.shrinkwrap.osgi.api.BndProjectBuilder;
import com.sohlman.liferay.arquillian.helper.CompanyTestHelper;
import com.sohlman.liferay.arquillian.helper.GroupTestHelper;
import com.sohlman.liferay.arquillian.helper.ServiceContextTestHelper;
import com.sohlman.liferay.arquillian.helper.UserTestHelper;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class DocumentAndMediaTest {
	
	@Deployment
	public static JavaArchive create() {
		BndProjectBuilder bndProjectBuilder = ShrinkWrap.create(
			BndProjectBuilder.class);

		bndProjectBuilder.setBndFile(new File("bnd-basic-portlet-test.bnd"));

		bndProjectBuilder.generateManifest(true);

		return bndProjectBuilder.as(JavaArchive.class);
	}

	@Before
	public void setup() throws Exception {
		Group group = _groupTestHelper.getDefeaultCompanyGuestGroup();
		User user = _userTestHelper.getAdminUser(group.getCompanyId());
		PermissionChecker permissionChecker = PermissionCheckerFactoryUtil
				.create(user);
		PermissionThreadLocal.setPermissionChecker(permissionChecker);
		PrincipalThreadLocal.setName(user.getUserId());
		List<FileEntry> fileEntries = _dlAppService.getFileEntries(
				group.getGroupId(), DLFolderConstants.DEFAULT_PARENT_FOLDER_ID);

		for (FileEntry fileEntry : fileEntries) {
			_dlAppService.deleteFileEntry(fileEntry.getFileEntryId());
		}
	}

	@After
	public void tearDown() {

	}

	@Test
	public void testAddFileToDocumentAndMedia() throws Exception {
		Group group = _groupTestHelper.getDefeaultCompanyGuestGroup();
		User user = _userTestHelper.getAdminUser(group.getCompanyId());

		PermissionChecker permissionChecker = PermissionCheckerFactoryUtil
				.create(user);
		PermissionThreadLocal.setPermissionChecker(permissionChecker);
		PrincipalThreadLocal.setName(user.getUserId());

		FileEntry fileEntry = addFileEntry(user.getUserId(),
				group.getGroupId(), 0, getFile("/Lorem.pdf"));

		_log.fatal(fileEntry.getTitle() + "  " + fileEntry.getSize());

		_dlAppService.deleteFileEntry(fileEntry.getFileEntryId());
	}

	@Test
	public void testAddFolderToDocumentAndMedia() throws Exception {
		Group group = _groupTestHelper.getDefeaultCompanyGuestGroup();
		User user = _userTestHelper.getAdminUser(group.getCompanyId());

		PermissionChecker permissionChecker = PermissionCheckerFactoryUtil
				.create(user);
		PermissionThreadLocal.setPermissionChecker(permissionChecker);
		PrincipalThreadLocal.setName(user.getUserId());

		String name = "Folder name";
		String description = "Folder description";
		long repositoryId = group.getGroupId();
		ServiceContext serviceContext = _serviceContextTestHelper
				.getServiceContext(group.getGroupId());

		_dlAppService.addFolder(repositoryId,
				DLFolderConstants.DEFAULT_PARENT_FOLDER_ID, name, description,
				serviceContext);

		Folder folder1 = _dlAppService.getFolder(repositoryId,
				DLFolderConstants.DEFAULT_PARENT_FOLDER_ID, name);

		Assert.assertEquals(folder1.getName(), name);

		_dlAppService.deleteFolder(repositoryId,
				DLFolderConstants.DEFAULT_PARENT_FOLDER_ID, name);

		try {
			_dlAppService.getFolder(repositoryId,
					DLFolderConstants.DEFAULT_PARENT_FOLDER_ID, name);
			Assert.fail("folder should not exist");
		} catch (Exception e) {
			Assert.assertEquals(e.getClass(), NoSuchFolderException.class);
		}
	}

	@Test
	public void testUpdateFileToDocumentAndMedia() throws Exception {
		Group group = _groupTestHelper.getDefeaultCompanyGuestGroup();
		User user = _userTestHelper.getAdminUser(group.getCompanyId());

		PermissionChecker permissionChecker = PermissionCheckerFactoryUtil
				.create(user);
		PermissionThreadLocal.setPermissionChecker(permissionChecker);
		PrincipalThreadLocal.setName(user.getUserId());
		
		File loremFile = getFile("/Lorem.pdf");
		File loremEsFile = getFile("/LoremEs.pdf");

		FileEntry fileEntry = addFileEntry(user.getUserId(),
				group.getGroupId(), 0, loremFile);

		fileEntry = updateFileEntry(fileEntry.getFileEntryId(), loremEsFile);
		
		fileEntry = _dlAppService.getFileEntry(fileEntry.getFileEntryId());
		
		
		byte[] loremEsBytes = FileUtil.getBytes(loremEsFile);
		byte[] filentryBytes = FileUtil.getBytes(fileEntry.getContentStream());
		
 		
		Assert.assertArrayEquals(loremEsBytes, filentryBytes);
	}

	public FileEntry addFileEntry(long userId, long groupId, long folderId,
			File file) throws Exception {
		// find out the MimeType
		
		String mimeType = MimeTypesUtil.getContentType(file.getName());
		
		String title = file.getName();
		String sourceFileName = file.getName();

		int workflowAction = WorkflowConstants.ACTION_PUBLISH;

		ServiceContext serviceContext = _serviceContextTestHelper
				.getServiceContext(groupId);
		
		serviceContext.setWorkflowAction(workflowAction);
		return _dlAppService.addFileEntry(groupId,
				DLFolderConstants.DEFAULT_PARENT_FOLDER_ID, sourceFileName,
				mimeType, title, "description", "changeLog", file,
				serviceContext);
	}

	protected FileEntry updateFileEntry(long fileEntryId,
			File file) throws Exception {
		FileEntry fileEntry = _dlAppService.getFileEntry(fileEntryId);
		
		// find out the MimeType
		
		String mimeType = MimeTypesUtil.getContentType(file.getName());
		
		String title = file.getName();
		String sourceFileName = file.getName();
		

		int workflowAction = WorkflowConstants.ACTION_PUBLISH;

		ServiceContext serviceContext = _serviceContextTestHelper
				.getServiceContext(fileEntry.getGroupId());
		
		serviceContext.setWorkflowAction(workflowAction);
		boolean majorVersion = false;
		return _dlAppService.updateFileEntry(fileEntryId, sourceFileName, mimeType, title, "description", "changeLog", majorVersion, file, serviceContext);
	}
	
	@Inject
	private CompanyTestHelper _companyTestHelper;
	
	@Inject
	private ServiceContextTestHelper _serviceContextTestHelper;
	
	@Inject 
	private CompanyLocalService _companyLocalService;
	
	@Inject
	private UserTestHelper _userTestHelper;
	
	@Inject
	private GroupTestHelper _groupTestHelper;
	
	@Inject DLAppService _dlAppService;
	
	protected File getFile(String fileNameAtClassPath) throws URISyntaxException {
		URL url = this.getClass().getResource(fileNameAtClassPath);
		return new File(url.toURI());
	}

	private Company _company;
	private Log _log = LogFactoryUtil.getLog(DocumentAndMediaTest.class);
}
