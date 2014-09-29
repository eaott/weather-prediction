import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;

import javax.imageio.ImageIO;

@Deprecated
public class CropImages {

	public static void main(String[] args) throws Throwable{		
		File dir = new File("data");
		if (!dir.exists())
			return;

		String[] files = dir.list(new FilenameFilter(){
			@Override
			public boolean accept(File dir, String name) {
				if (!name.contains(".gif") || name.contains("crop"))
					return false;
				
				String cropName = String.format("%s_crop.gif", name.substring(0, name.lastIndexOf(".gif")));

				return !new File(dir, cropName).exists();
			}});
		for (String f : files)
		{
			System.out.println(f);
			File orig = new File(dir, f);
			BufferedImage img = ImageIO.read(orig);			
			
			int x = 131;
			int y = 127;
			int width = 350;
			int height = 300;
			
			BufferedImage crop = img.getSubimage(x, y, width, height);
			String filename = String.format("%s_crop.gif", f.substring(0, f.lastIndexOf(".gif")));
			ImageIO.write(crop, "gif", new File(dir, filename));
			
			Files.deleteIfExists(orig.toPath());
		}
	}

}
