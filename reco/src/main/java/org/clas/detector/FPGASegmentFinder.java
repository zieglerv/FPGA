package org.clas.detector;

import java.io.FileNotFoundException;

import org.jlab.coda.jevio.EvioException;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;

public class FPGASegmentFinder {
/**
 * Algorithm by Mac Mestayer
 * 
 *  This program finds track segments in a 6-layer superlayer.  It first finds good 'triangles'
 *  A 'triangle' is a logical grouping of 6 wires; for example a 'bottom triangle' consists
 *  of wire number "i" in layer 3, wire numbers "i" and "i-1" in layer 2 and wire numbers "i",
 *  "i-1" and "i+1" in layer 1.  A triangle is "true" if it has at least one hit in at least
 *  2 layers.  A triangle's identifying index is the wire number of layer 3 (for bottom) or 
 *  layer 4 (for top).
 *  A segment is a coincidence between a top and bottom triangle with the same wire number, or
 *  with the top's wire number being one larger than the bottom's.
 */
	public FPGASegmentFinder() {
		// TODO Auto-generated constructor stub
	}
	
	private boolean _HitArray[][][][] 					= new boolean[6][6][6][112];
	private boolean _BottomTriangleComponents[][][][] 	= new boolean[6][6][112][3];
	private boolean _TopTriangleComponents[][][][] 		= new boolean[6][6][112][3];
	private boolean _BottomTriangle[][][][] 			= new boolean[6][6][112][3];
	private boolean _TopTriangle[][][][] 				= new boolean[6][6][112][3];
	private boolean _SegmentComponents[][][][][] 		= new boolean[6][6][112][3][3];
	private boolean _Segment[][][] 						= new boolean[6][6][112];
	public boolean[][][] get_Segment() {
		return _Segment;
	}

	public void set_Segment(boolean[][][] _Segment) {
		this._Segment = _Segment;
	}

	public void FillHitArray(DataEvent event) {
		if(event.hasBank("DC::tdc")==false) {
			//System.err.println("there is no dc bank ");		
			return;
		}
		_HitArray = new boolean[6][6][6][112];
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
			_HitArray[sector[i]-1][superlayerNum[i]-1][layerNum[i]-1][wire[i]-1]=true;
			
		}
	}
	
	public void BuildPatterns() {
		_BottomTriangleComponents	= new boolean[6][6][112][3];
		_TopTriangleComponents 		= new boolean[6][6][112][3];
		_BottomTriangle 			= new boolean[6][6][112][3];
		_TopTriangle 				= new boolean[6][6][112][3];
		_SegmentComponents 			= new boolean[6][6][112][3][3];
		_Segment 					= new boolean[6][6][112];
		
		for(int isec = 0; isec < 6; isec++) {
			for(int islr = 0; islr < 6; islr++) {
				for(int iwir = 0; iwir < 112; iwir++) {
					int iwirmin2 = iwir - 2;
					if(iwirmin2<0)
						iwirmin2 =0;
					int iwirmin1 = iwir - 1;
					if(iwirmin1<0)
						iwirmin1 =0;
					int iwirplus1 = iwir + 1;
					if(iwirplus1>111)
						iwirplus1 =111;
					_BottomTriangleComponents[isec][islr][iwir][0] = (_HitArray[isec][islr][0][iwirmin1] 
							|| _HitArray[isec][islr][0][iwir] 
							|| _HitArray[isec][islr][0][iwirplus1]);
					_BottomTriangleComponents[isec][islr][iwir][1] = (_HitArray[isec][islr][1][iwirmin1] 
							|| _HitArray[isec][islr][1][iwir]);
					_BottomTriangleComponents[isec][islr][iwir][2] = _HitArray[isec][islr][2][iwir];
					
					_TopTriangleComponents[isec][islr][iwir][0] = _HitArray[isec][islr][3][iwir]; 
					_TopTriangleComponents[isec][islr][iwir][1] = (_HitArray[isec][islr][4][iwir]
							|| _HitArray[isec][islr][4][iwirplus1]); 
					_TopTriangleComponents[isec][islr][iwir][2] = (_HitArray[isec][islr][5][iwirmin1]
							|| _HitArray[isec][islr][5][iwir]
							|| _HitArray[isec][islr][5][iwirplus1]); 
					
					//this triangle has hits in all 3 layers
					_BottomTriangle[isec][islr][iwir][2] = ( 
							_BottomTriangleComponents[isec][islr][iwir][0] 
							&& _BottomTriangleComponents[isec][islr][iwir][1]
							&& _BottomTriangleComponents[isec][islr][iwir][2]);
					//this triangle has hits in only 2 layers
					_BottomTriangle[isec][islr][iwir][1] = ( 
							(
							(_BottomTriangleComponents[isec][islr][iwir][0] 
							&& _BottomTriangleComponents[isec][islr][iwir][1]) || 
							(_BottomTriangleComponents[isec][islr][iwir][0]
							&& _BottomTriangleComponents[isec][islr][iwir][2]) || 
							(_BottomTriangleComponents[isec][islr][iwir][1]
							&& _BottomTriangleComponents[isec][islr][iwir][2])
							) && !_BottomTriangle[isec][islr][iwir][2]
									);				
						//this triangle has hits in only 1 layer
					_BottomTriangle[isec][islr][iwir][0] = ( 
							(
							(_BottomTriangleComponents[isec][islr][iwir][0] 
							|| _BottomTriangleComponents[isec][islr][iwir][1]) 
							|| _BottomTriangleComponents[isec][islr][iwir][2]
							) && !_BottomTriangle[isec][islr][iwir][2]
									);

					//this triangle has hits in all 3 layers
					_TopTriangle[isec][islr][iwir][2] = ( 
							_TopTriangleComponents[isec][islr][iwir][0] 
							&& _TopTriangleComponents[isec][islr][iwir][1]
							&& _TopTriangleComponents[isec][islr][iwir][2]);
					//this triangle has hits in only 2 layers
					_TopTriangle[isec][islr][iwir][1] = ( 
							(
							(_TopTriangleComponents[isec][islr][iwir][0] 
							&& _TopTriangleComponents[isec][islr][iwir][1]) || 
							(_TopTriangleComponents[isec][islr][iwir][0]
							&& _TopTriangleComponents[isec][islr][iwir][2]) || 
							(_TopTriangleComponents[isec][islr][iwir][1]
							&& _TopTriangleComponents[isec][islr][iwir][2])
							) && !_TopTriangle[isec][islr][iwir][2]
									);				
						//this triangle has hits in only 1 layer
					_TopTriangle[isec][islr][iwir][0] = ( 
							(
							(_TopTriangleComponents[isec][islr][iwir][0] 
							|| _TopTriangleComponents[isec][islr][iwir][1]) 
							|| _TopTriangleComponents[isec][islr][iwir][2]
							) && !_TopTriangle[isec][islr][iwir][2]
									);
				}
				
				for(int iwir = 0; iwir < 112; iwir++) {
				
					int iwirmin2 = iwir - 2;
					if(iwirmin2<0)
						iwirmin2 =0;					
					int iwirmin1 = iwir - 1;
					if(iwirmin1<0)
						iwirmin1 =0;
					int iwirplus1 = iwir + 1;
					if(iwirmin1>111)
						iwirmin1 =111; 
					
					for(int ti = 0; ti<3; ti++) {
						for(int bi = 0; bi<3; bi++) {
							if( (ti==1 && bi==0 ) || (ti==0 && bi==1 ) )
								continue;
					_SegmentComponents[isec][islr][iwir][ti][bi] = ( 
							(_TopTriangle[isec][islr][iwir][ti] 
							&& _BottomTriangle[isec][islr][iwir][bi] ) 
							|| (_TopTriangle[isec][islr][iwir][ti] 
							&& _BottomTriangle[isec][islr][iwirmin1][bi] ) 
							|| (_TopTriangle[isec][islr][iwir][ti] 
							&& _BottomTriangle[isec][islr][iwirmin2][bi] ) 
							|| ( _TopTriangle[isec][islr][iwir][ti]  
							&& _BottomTriangle[isec][islr][iwirplus1][bi] )
							);
						}
					}
					
					_Segment[isec][islr][iwir] = _SegmentComponents[isec][islr][iwir][2][2] 
							                  || _SegmentComponents[isec][islr][iwir][2][1] 
									          || _SegmentComponents[isec][islr][iwir][1][2]
									          || _SegmentComponents[isec][islr][iwir][1][1]
											  || _SegmentComponents[isec][islr][iwir][0][2]
											  || _SegmentComponents[isec][islr][iwir][2][0];
				}
			}
		}
				
	}
	
	public void ReadPatterns() {
		for(int isec = 0; isec < 6; isec++) {
			for(int islr = 0; islr < 6; islr++) {
				for(int iwir = 0; iwir < 112; iwir++) {
					if(_Segment[isec][islr][iwir])
						System.out.println("Wire "+(iwir+1)+" is in Segment located in Sector "+(isec+1)+", Superlayer "+(islr+1));
				}
			}
		}
	}
	
	
	
	public static void main(String[] args) throws FileNotFoundException, EvioException{
		
		String inputFile = "/Users/ziegler/clas12_000797_a00000.hipo";
		//String inputFile = "/Users/ziegler/Workdir/Distribution/coatjava-4a.0.0/e2to6hipo.hipo";
		
		//String inputFile = args[0];
		//String outputFile = args[1];
		
		System.err.println(" \n[PROCESSING FILE] : " + inputFile);
		
		FPGASegmentFinder en = new FPGASegmentFinder();
		
		int counter = 0;
		
		 HipoDataSource reader = new HipoDataSource();
         reader.open(inputFile);
		
		
		long t1=0;
		while(reader.hasEvent() ){
			
			counter++;
		
			DataEvent event = reader.getNextEvent();
			if(counter>0)
				t1 = System.currentTimeMillis();
			en.FillHitArray(event);
			en.BuildPatterns();
			en.ReadPatterns();
			//System.out.println("  EVENT "+counter);
			if(counter>3) break;
			//event.show();
			//if(counter%100==0)
			System.out.println("*************************************************************run "+counter+" events");
			
		}
		//writer.close();
		double t = System.currentTimeMillis()-t1;
		System.out.println(t1+" TOTAL  PROCESSING TIME = "+(t/(float)counter));
	 }

	

}
