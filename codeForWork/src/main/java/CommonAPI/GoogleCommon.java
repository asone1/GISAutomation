package CommonAPI;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.Permission;

public class GoogleCommon {
	private static Permission insertPermission(Drive service, String fileId) throws Exception{
		   Permission newPermission = new Permission();
		   newPermission.setType("anyone");
		   newPermission.setRole("writer");
//		   newPermission.setValue("");
//		   newPermission.setWithLink(true);
		   return service.permissions().create(fileId, newPermission).execute();
		}
}
