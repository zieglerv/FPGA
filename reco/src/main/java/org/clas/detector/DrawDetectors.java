package org.clas.detector;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.jlab.detector.base.DetectorType;
import org.jlab.detector.view.DetectorPane2D;
import org.jlab.detector.view.DetectorShape2D;

public class DrawDetectors {

	public DrawDetectors() {
		// TODO Auto-generated constructor stub
	}
	
	

	public void drawDC(List<DetectorPane2D> detectorViewDC) {
		for(int s =0; s< 6; s++) {			
			for(int slrnum = 0; slrnum <6; slrnum++) {
				for(int lrnum = 0; lrnum <6; lrnum++) {
					for(int w = 0; w <112; w++) {
						DetectorShape2D module = new DetectorShape2D(DetectorType.DC,slrnum,lrnum,w);					
						module.createBarXY(2, 2);					
						module.getShapePath().translateXYZ(lrnum*2+(12+2)*slrnum, w, 0);					
						module.setColor(255,250,240); 
						//module.setColor(255,182,255); 
						detectorViewDC.get(s).getView().addShape("DC", module);		
					}
				}		
			}
			detectorViewDC.get(s).setName("sector "+(s+1)); 
		}
    
		
		
		//detectorViewDC.updateBox();
	}

	
	public void makePanels(List<DetectorPane2D> detectorPanels,
			JTabbedPane tabbedpane) {
		
		for(int i=0; i< detectorPanels.size(); i++) {
			JPanel pane = new JPanel();
			pane.setLayout(new BorderLayout());
			//JSplitPane   splitPane = new JSplitPane();
			//splitPane.setLeftComponent(detectorPanels.get(i));
			//EmbeddedCanvas canvas = new EmbeddedCanvas();
			
			//splitPane.setRightComponent(canvas);
			//pane.add(splitPane,BorderLayout.CENTER);
			pane.add(detectorPanels.get(i));
			tabbedpane.add(pane, detectorPanels.get(i).getName());
			
			//canvases.put(detectorPanels.get(i).getName(), canvas);
			
		}
        
	}

	
    
}
