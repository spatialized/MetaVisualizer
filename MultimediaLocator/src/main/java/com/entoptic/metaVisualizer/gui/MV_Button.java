package main.java.com.entoptic.metaVisualizer.gui;

import main.java.com.entoptic.metaVisualizer.world.WMV_World;
//import processing.core.PApplet;
import processing.core.PVector;

/**
 * Button class
 * Constructor Note: Use rectMode(CORNER) before displaying
 * @author davidgordon
 */
public class MV_Button
{
	private int id;
	private String text;
	private String label = "";
	private boolean visible = true;
	
	private float textSize;
	private boolean selected;
	public float leftEdge, rightEdge, topEdge, bottomEdge;
	
	/**
	 * Constructor for button (Note: Use rectMode(CORNER) before displaying)
	 * @param newID
	 * @param newText
	 * @param newTextSize
	 * @param newLeftEdge
	 * @param newRightEdge
	 * @param newTopEdge
	 * @param newBottomEdge
	 */
	MV_Button(int newID, String newText, float newTextSize, float newLeftEdge, float newRightEdge, float newTopEdge, float newBottomEdge)
	{
//		System.out.println("ML_Button.ML_Button(): "+newText+"  newTopEdge:"+newTopEdge);
		id = newID;
		text = newText;
		textSize = newTextSize;
		
		leftEdge = newLeftEdge;
		rightEdge = newRightEdge;
		topEdge = newTopEdge;
		bottomEdge = newBottomEdge;
	}

	public int getID()
	{
		return id;
	}
	
	public boolean isSelected()
	{
		return selected;
	}
	
	public boolean isVisible()
	{
		return visible;
	}
	
	public void setVisible(boolean newState)
	{
		visible = newState;
	}
	
	public boolean containsPoint(PVector point)
	{
		if( point.x > leftEdge && point.x < rightEdge && 
				point.y > topEdge && point.y < bottomEdge )
			return true;
		else
			return false;
	}
	
	public void setSelected(boolean newState)
	{
		selected = newState;
	}
	
	public void addLabel(String newLabel)
	{
		label = newLabel;
	}
	
	public void clearLabel()
	{
		label = "";
	}
	
	/**
	 * Display button
	 * @param p Parent world
	 * @param hue
	 * @param saturation
	 * @param brightness
	 */
	public void display(WMV_World p, float hue, float saturation, float brightness)
	{
		if(visible)
		{
//			p.ml.rectMode(PApplet.CORNER);				// Specify top left point in rect()
			float textWidthFactor = 2.f / textSize;
			float textHeightFactor = 5.f / textSize;

			p.mv.stroke(hue, saturation, brightness, 255);												
			p.mv.strokeWeight(3.f);

			p.mv.pushMatrix();

			p.mv.fill(hue, saturation, brightness, 255);												
			p.mv.textSize(textSize);

			float xOffset = -textWidthFactor * text.length();  
			p.mv.text(text, (rightEdge+leftEdge)/2.f + xOffset, topEdge + p.mv.display.buttonHeight / 2.f - textHeightFactor / 2.f, 0);

			if(!label.equals(""))
			{
				xOffset = -textWidthFactor * label.length() * 2.f - p.mv.display.buttonSpacing;  
				p.mv.text(label, leftEdge + xOffset, topEdge + p.mv.display.buttonHeight / 2.f - textHeightFactor / 2.f, 0);
			}

			p.mv.popMatrix();

			p.mv.pushMatrix();
			if(selected)
			{
				p.mv.strokeWeight(1.f);
				p.mv.fill(hue, saturation, 155, 125);
			}
			else
			{
				p.mv.strokeWeight(0.f);
				p.mv.fill(hue, saturation, 75, 55);
			}

			float width = rightEdge-leftEdge;
			float height = bottomEdge-topEdge;

//			p.ml.rect(leftEdge-width/2.f, topEdge-height/2.f, width, height);
			p.mv.rect(leftEdge, topEdge, width, height);
			p.mv.popMatrix();
//			p.ml.rectMode(PApplet.CENTER);				// Return rect mode to Center
		}
	}
}
