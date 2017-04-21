package org.clas.detector;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.jlab.detector.base.DetectorType;
import org.jlab.detector.decode.CodaEventDecoder;
import org.jlab.detector.decode.DetectorEventDecoder;
import org.jlab.detector.view.DetectorListener;
import org.jlab.detector.view.DetectorPane2D;
import org.jlab.detector.view.DetectorShape2D;
import org.jlab.geom.prim.Point3D;
import org.jlab.groot.data.H1F;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.task.DataSourceProcessorPane;
import org.jlab.io.task.IDataEventListener;

/**
 *
 * @author ziegler
 */
public class EventViewer implements IDataEventListener, DetectorListener {
    
	List<DetectorPane2D> DetectorPanels 	= new ArrayList<DetectorPane2D>();
	List<DetectorPane2D> DetectorPanels2 	= new ArrayList<DetectorPane2D>();
	JTabbedPane tabbedpane           		= null;
	JPanel mainPanel 						= null;
	DataSourceProcessorPane processorPane 	= null;

    DrawDetectors drawDets					= null;
   
    FPGASegmentFinder en = new FPGASegmentFinder();
	
    
    TreeMap<String, List<H1F>>  histos = new TreeMap<String,List<H1F>>();
    
    public EventViewer() {
    	
		mainPanel = new JPanel();
		
		mainPanel.setLayout(new BorderLayout());
		
		tabbedpane 		= new JTabbedPane();
        drawDets 		= new DrawDetectors();
        
        for(int i =0; i<6; i++) {
        	DetectorPanels.add(new DetectorPane2D());
        	
        }
        drawDets.drawDC(DetectorPanels);
       
        drawDets.makePanels(DetectorPanels, tabbedpane); 
        
        mainPanel.add(tabbedpane);
        processorPane = new DataSourceProcessorPane();
        mainPanel.add(processorPane,BorderLayout.PAGE_END);
        
        for(int k =0; k<this.DetectorPanels.size(); k++) {
        	this.DetectorPanels.get(k).getView().addDetectorListener(this);
        }
        this.processorPane.addEventListener(this);
       
		
    }
   
    public void PlotAllHits(DataEvent event) {
    	//
    	en.FillHitArray(event);
		en.BuildPatterns();
		
		if(event.hasBank("DC::tdc")==false) {
			System.err.println("there is no dc bank ");		
			return;
		}
		
		drawDets.drawDC(DetectorPanels);
		
		DataBank bankDGTZ = event.getBank("DC::tdc");
		
		int rows = bankDGTZ.rows();
		int[] sector = new int[rows];
		int[] layer = new int[rows];
		int[] superlayerNum = new int[rows];
		int[] layerNum = new int[rows];
		int[] wire = new int[rows];
		
		
		for(int i = 0; i< rows; i++) {
			sector[i] = bankDGTZ.getByte("sector", i);
			layer[i] = bankDGTZ.getByte("layer", i);
			wire[i] = bankDGTZ.getShort("component", i);
			superlayerNum[i]=(layer[i]-1)/6 + 1;
			layerNum[i] = layer[i] - (superlayerNum[i] - 1)*6; 
			
			DetectorShape2D module = new DetectorShape2D(DetectorType.DC,superlayerNum[i]-1,layerNum[i]-1, wire[i] -1);					
			module.createBarXY(2, 2);					
			module.getShapePath().translateXYZ((layerNum[i]-1)*2+(12+2)*(superlayerNum[i]-1), wire[i] -1, 0);								
			module.setColor(0,0,255); 
			
			DetectorPanels.get(sector[i]-1).getView().addShape("DC", module);	
			//System.out.println(" Raw hit in sector "+sector[i]+" superlayer "+superlayerNum[i]+" layer "+layerNum[i]+" wire = "+wire[i]);
			DetectorPanels.get(sector[i]-1).repaint();
			
		}
		
		for(int isec = 0; isec < 6; isec++) {
			for(int islr = 0; islr < 6; islr++) {
				for(int iwir = 0; iwir < 112; iwir++) {
					if(en.get_Segment()[isec][islr][iwir]) {
						System.out.println("FPGA Algo: Wire "+(iwir+1)+" is in Segment located in Sector "+(isec+1)+", Superlayer "+(islr+1));
						DetectorShape2D module = new DetectorShape2D(DetectorType.DC,islr,0, iwir);					
						module.createBarXY(2, 2);					
						module.getShapePath().translateXYZ((1-1)*2+(12+2)*(islr), iwir, 0);								
						module.setColor(0,255,255); 	//cyan				
						DetectorPanels.get(isec).getView().addShape("DC", module);	
						DetectorPanels.get(isec).repaint();
					}
					if(en.get_LeftSegment()[isec][islr][iwir]) {
						System.out.println("FPGA Algo: Wire "+(iwir+1)+" is in Left-Segment located in Sector "+(isec+1)+", Superlayer "+(islr+1));
						DetectorShape2D module = new DetectorShape2D(DetectorType.DC,islr,0, iwir);					
						module.createBarXY(2, 2);					
						module.getShapePath().translateXYZ((1-1)*2+(12+2)*(islr), iwir, 0);								
						module.setColor(0,255,127); 	//green				
						DetectorPanels.get(isec).getView().addShape("DC", module);	
						DetectorPanels.get(isec).repaint();
					}
					if(en.get_RightSegment()[isec][islr][iwir]) {
						System.out.println("FPGA Algo: Wire "+(iwir+1)+" is in Right-Segment located in Sector "+(isec+1)+", Superlayer "+(islr+1));
						DetectorShape2D module = new DetectorShape2D(DetectorType.DC,islr,0, iwir);					
						module.createBarXY(2, 2);					
						module.getShapePath().translateXYZ((1-1)*2+(12+2)*(islr), iwir, 0);								
						module.setColor(255,105,180); 	//pink				
						DetectorPanels.get(isec).getView().addShape("DC", module);	
						DetectorPanels.get(isec).repaint();
					}
					if(en.get_CenterSegment()[isec][islr][iwir]) {
						System.out.println("FPGA Algo: Wire "+(iwir+1)+" is in Center-Segment located in Sector "+(isec+1)+", Superlayer "+(islr+1));
						DetectorShape2D module = new DetectorShape2D(DetectorType.DC,islr,0, iwir);					
						module.createBarXY(2, 2);					
						module.getShapePath().translateXYZ((1-1)*2+(12+2)*(islr), iwir, 0);								
						module.setColor(160,32,240); 	 	//purple			
						DetectorPanels.get(isec).getView().addShape("DC", module);	
						DetectorPanels.get(isec).repaint();
					}
				}
			}
			//DetectorPanels.get(isec).repaint();
		}
		
		
		
		
	}
    public JPanel  getPanel(){
        return mainPanel;
    }

    @Override
    public void dataEventAction(DataEvent event) {
    	
		if(event!=null ){
			this.PlotAllHits(event);
		}
    }

    
    
    public void clearHistograms(){
        
    }
    
    
    public void analyzeData(){
        
    }
    
    public void fillhistograms(){
        
    }
    
    @Override
    public void timerUpdate() {
        
    }

    @Override
    public void resetEventListener() {
        
    }
    
    @Override
    public void processShape(DetectorShape2D shape) {
        System.out.println("SHAPE SELECTED = " + shape.getDescriptor());
        
    }
    
    public static void main(String[] args){
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        EventViewer viewer = new EventViewer();
        //frame.add(viewer.getPanel());
        frame.add(viewer.mainPanel);
        frame.setSize(900, 600);
        frame.setVisible(true);
    }

   
}