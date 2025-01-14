package zeiss.PALM;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;

/**
 * This class is designed to handle the calibration informations required to read/write Zeiss PALM text files
 * @author Fabrice P. Cordelières, fabrice.cordelieres@gmail.com
 *
 */
public class Zeiss_PALM_Image_Infos {
	/** Image width, in microns **/
	double sizeXmicrons=248.1;
	
	/** Image height, in microns **/
	double sizeYmicrons=185.7;
	
	/** Image width, in pixels **/
	double sizeXpixels=1388;
	
	/** Image height, in pixels **/
	double sizeYpixels=1038;
	
	/** Stage X position **/
	double stagePositionX=68220.0;
	
	/** Stage Y position **/
	double stagePositionY=36565.0;
	
	/** Zero stage X position **/
	double zeroStagePositionX=118;
	
	/** Zero stage Y position **/
	double zeroStagePositionY=-30;
	
	
	/** Stage position, as a Zeiss_PALM_Point **/
	Zeiss_PALM_Point position;
	
	/** Image dimensions, as a Zeiss_PALM_Point **/
	Zeiss_PALM_Point imageDimensions;
	
	/** Image calibration, in microns/pixel, as a Zeiss_PALM_Point **/
	Zeiss_PALM_Point calibration;
	
	/** Zero stage position, as a Zeiss_PALM_Point **/
	Zeiss_PALM_Point zeroPosition;
	
	/**
	 * Tries to read the calibration informations form the current image, otherwise displays a dialog box
	 * @return true in case the dialog was Oked, false otherwise
	 */
	public boolean read(){
		ImagePlus ip = WindowManager.getCurrentImage();
		boolean readInfoFromImage=false;
		
		String infos=ip!=null?ip.getInfoProperty():""; //Caution: contains an invisible and annoying character !!!
		
		if(infos!=null){
			infos=infos.replace(" ", "");
			readInfoFromImage=infos.indexOf("PALMRobo")!=-1;
		}
		
		if(readInfoFromImage) readParametersFromImage(infos);
		if(!GUI(!readInfoFromImage)) return false;
		
		return true;
	}
	
	/**
	 * Reads parameters from the image description metadata field
	 * @param infos the image description metadata field
	 */
	private void readParametersFromImage(String infos){
		String field=getSection(infos, "SizeX Type=\"µm\"");
		if(field=="") {
			field=getSection(infos, "SizeX Type=\"�m\""); //Try to handle cases where the caracter set is not well recognised
		}
		sizeXmicrons=field.equals("")?Double.NaN:Double.parseDouble(field);
		field=getSection(infos, "SizeY Type=\"µm\"");
		if(field=="") {
			field=getSection(infos, "SizeY Type=\"�m\""); //Try to handle cases where the caracter set is not well recognised
		}
		sizeYmicrons=field.equals("")?Double.NaN:Double.parseDouble(field);
		field=getSection(infos, "SizeX Type=\"Pixel\"");
		sizeXpixels=field.equals("")?Double.NaN:Double.parseDouble(field);
		field=getSection(infos, "SizeY Type=\"Pixel\"");
		sizeYpixels=field.equals("")?Double.NaN:Double.parseDouble(field);
		field=getSection(infos, "StagePosition Type=\"X-coordinate\"");
		stagePositionX=field.equals("")?Double.NaN:Double.parseDouble(field);
		field=getSection(infos, "StagePosition Type=\"Y-coordinate\"");
		stagePositionY=field.equals("")?Double.NaN:Double.parseDouble(field);
		convertParameters();
	}
	
	/**
	 * Parse an input string based on the input section name
	 * @param input the string to parse
	 * @param sectionName the name of the section to look for
	 * @return a string representing the section
	 */
	private String getSection(String input, String sectionName){
		String startTag="<"+sectionName+">";
		int startTagLength=startTag.length();
		int startTagPosition=input.indexOf(startTag);
		
		if(sectionName.lastIndexOf(" ")!=-1) sectionName=sectionName.substring(0, sectionName.indexOf(" "));

		String stopTag="</"+sectionName+">";
		int stopTagPosition=input.indexOf(stopTag, startTagPosition+startTagLength);

		if(startTagPosition!=-1 && stopTagPosition!=-1){
			return input.substring(startTagPosition+startTagLength, stopTagPosition);
		}else{
			return "";
		}
	}
	
	/**
	 * Converts the parameters to Zeiss_PALM_Point for further use
	 */
	private void convertParameters(){
		position=new Zeiss_PALM_Point(stagePositionX, stagePositionY);
		imageDimensions=new Zeiss_PALM_Point(sizeXpixels, sizeYpixels);
		calibration=new Zeiss_PALM_Point((double) sizeXmicrons/sizeXpixels, (double) sizeYmicrons/sizeYpixels);
		zeroPosition=new Zeiss_PALM_Point(zeroStagePositionX, zeroStagePositionY);
	}
	
	/**
	 * GUI used to retrieve the calibration value,s in case they were not extracted from an image
	 * @param getParameters true if all the aprameters are to be retrieved from the user
	 * @return true in case the GUI was Oked, false otherwise
	 */
	private boolean GUI(boolean getParameters){
		GenericDialog gd=new GenericDialog("Zeiss PALM Roi IO");
		if(getParameters){
			gd.addNumericField("SizeX_(microns)", sizeXmicrons, 3);
			gd.addNumericField("SizeY_(microns)", sizeYmicrons, 3);
			gd.addNumericField("SizeX_(pixels)", sizeXpixels, 3);
			gd.addNumericField("SizeY_(pixels)", sizeYpixels, 3);
			gd.addNumericField("StagePosition_X-coordinate", stagePositionX, 3);
			gd.addNumericField("StagePosition_Y-coordinate", stagePositionY, 3);
		}
		gd.addNumericField("Zero_StagePosition_X-coordinate", zeroStagePositionX, 3);
		gd.addNumericField("Zero_StagePosition_Y-coordinate", zeroStagePositionY, 3);
		gd.showDialog();
		
		if(gd.wasOKed()){
			if(getParameters){
				sizeXmicrons=gd.getNextNumber();
				sizeYmicrons=gd.getNextNumber();
				sizeXpixels=gd.getNextNumber();
				sizeYpixels=gd.getNextNumber();
				stagePositionX=gd.getNextNumber();
				stagePositionY=gd.getNextNumber();
			}
			zeroStagePositionX=gd.getNextNumber();
			zeroStagePositionY=gd.getNextNumber();
			
			convertParameters();
			return true;
		}else{
			return false;
		}
	}
}
