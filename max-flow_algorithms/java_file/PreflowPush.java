/**
 *
 * @author fangchen
 */

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.*;
import java.util.Set;
import java.util.LinkedList;


public class PreflowPush {
	
	public SimpleGraph G; //the input graph G
	public Double f; //flow value 
	public HashMap<String, Integer> height; //height of each vertex, can be 0 to 2n-1
	public HashMap<String, Double> excess; //excess of each vertex, is sum of flow value into v minus sum of flow value out v
	public SimpleGraph Gf;  //residual graph
	public LinkedList edgeList_withflow; //the edges list which has the same edges as G, the 'data' field is the flow value in G
	
	/* the Constructor
	 * 1. construct members
	 * 2. create resudial graph Gf
	 */
	public PreflowPush(SimpleGraph G){
		this.G=G;
		this.f = 0.0; 				//f(v) = 0;
		
		height = new HashMap<String, Integer>();
		Iterator i;
		Vertex v;
		int height_value_zero = 0;
		for(i=this.G.vertices();i.hasNext();){
			v = (Vertex)i.next();
			this.height.put(v.getName().toString(), height_value_zero);
		}
		
		excess = new HashMap<String, Double>();
		Double excess_value_zero = 0.0;
		for(i=this.G.vertices();i.hasNext();){
			v = (Vertex)i.next();
			this.excess.put(v.getName().toString(), excess_value_zero);
		}
			
		this.Gf = new SimpleGraph(); //create residual graph, the same as G when f(v) = 0
		for(i=G.vertices();i.hasNext();){  //add vertices to Gf
			v = (Vertex)i.next();
			Vertex vertex = new Vertex(v.getData(),v.getName());
					
			this.Gf.insertVertex(vertex.getData(), vertex.getName());
		}
		
		Edge currentedge;
		for(i=G.edges();i.hasNext();){ //add edges to Gf
			currentedge = (Edge)i.next();  //get an edge e(v1,v2) from G
			//find v1, v2 in Gf
			Iterator j;
			Vertex currentvertex;
			Vertex v1=null;
			Vertex v2=null;
			for(j=Gf.vertices();j.hasNext();){
				currentvertex = (Vertex)j.next();
				if(currentvertex.getName() == currentedge.getFirstEndpoint().getName()){
					v1 = currentvertex;
				}
				if(currentvertex.getName() == currentedge.getSecondEndpoint().getName()){
					v2 = currentvertex;
				}
			}
			
			Object data = new Object();
			data = currentedge.getData();
			//create a new edge base on the v1, v2 of Gf
			Edge newedge = new Edge(v1,v2,data,"forward"); //all edges in Gf are 'forward' at the beginning
			this.Gf.edgeList.addLast(newedge); //add the new edge to Gf edgeList
			v1.incidentEdgeList.addLast(newedge);//this edge is incident edge of v1, add it to v1's list
			v2.incidentEdgeList.addLast(newedge);//this edge is incident edge of v2, add it to v2's list
			
		} //end add edges
		
		/*the edge list which is the same as G
		 *the 'data' field represents the flow value on that edge
		 *at the beginning, f(e)=0 for each e(v,w) in G
		 */
		this.edgeList_withflow = new LinkedList();
		for(i=G.edges();i.hasNext();){   
			currentedge = (Edge)i.next();
			Vertex v1 = new Vertex(currentedge.getFirstEndpoint().getData(), currentedge.getFirstEndpoint().getName());
			Vertex v2 = new Vertex(currentedge.getSecondEndpoint().getData(),currentedge.getSecondEndpoint().getName());
			Double data = 0.0;  //flow value
			Edge newedge =new Edge(v1,v2,data,"");
			this.edgeList_withflow.addLast(newedge);
		}
		
	}
	
	/* Initialization
	 * Initialize height, h(s) = n, others are 0
	 * Initialize flow value on each edge e(s,v), f(e)=Ce
	 * Initialize excess value base on the flow value on edges e(s,v)
	 */
	public void init(){
		//initialize height, h(s)=n, others are zero
		Iterator i;
		Vertex v;
		int height_value_zero=0;
		int n = this.G.numVertices();
		for(i=this.G.vertices();i.hasNext();){
			v = (Vertex)i.next();
			if(v.getName().toString().equals("s")){
				height.put("s", n);
			}
			else{
				height.put(v.getName().toString(), height_value_zero);
			}
		}
		
	
		//init flow value f(e) = Ce for all e(s,v), update e's value in edgeList_withflow
		Edge e;
		String v1name,v2name;
		for(i = this.edgeList_withflow.iterator();i.hasNext();){
			e = (Edge)i.next();
			v1name = e.getFirstEndpoint().getName().toString();
			v2name = e.getSecondEndpoint().getName().toString();
			if(v1name.equals("s")){
				Double capacity = getEdgeCapacity("s", v2name);
				e.setData(capacity);  //set that edge flow value f(e)=Ce
				updateResidualGraph(e.getFirstEndpoint(),e.getSecondEndpoint(),(Double)e.getData());  //since flow value changed on this edge e, update Gf base on e
				updateExcess(v1name,-(Double)e.getData());   //two vertices of this edge should be updated with new excess value
				updateExcess(v2name,(Double)e.getData());
			}
		}
		
		//update Gf base on the init flow value
		
		//update excess of some nodes base on the init flow value
		
		
	} //end init
	

	/*
	 * output Gf
	 * output height h
	 * output excess e
	 * output flow value on each edge
	 */
	public void print(){
		System.out.println("********************************");
		System.out.println("height:");
		
		for(Entry entry : this.height.entrySet()){
			System.out.println("h" + entry.getKey().toString() + ": " + entry.getValue().toString());
		}
		
		System.out.println("excess:");
		for(Entry entry : this.excess.entrySet()){
			System.out.println("e" + entry.getKey().toString() + ": " + entry.getValue().toString());
		}
		
		System.out.println("flow value on each edge in G:");
		Iterator i;
		Edge e;
		for(i=this.edgeList_withflow.iterator();i.hasNext();){
			e = (Edge)i.next(); 
			System.out.println(e.getFirstEndpoint().getName().toString() + " " + e.getSecondEndpoint().getName().toString() + " " + e.getData().toString());
		}
		
		System.out.println("edges on residual graph Gf:");
		for(i=this.Gf.edges();i.hasNext();){
			e = (Edge)i.next();
			System.out.println(e.getFirstEndpoint().getName().toString() + " " + e.getSecondEndpoint().getName().toString() + " " + e.getName().toString() + " " + e.getData().toString());
			
		}
		
		/*
		System.out.println("incident edges of each vertex on residual graph Gf:");
		Vertex v;
		for (i= this.Gf.vertices(); i.hasNext(); ) {
            v = (Vertex) i.next();
            System.out.println("Vertex "+v.getName());
            
            Iterator j;
            for (j = G.incidentEdges(v); j.hasNext();) {
                e = (Edge) j.next();
                System.out.println("  " + e.getFirstEndpoint().getName().toString() + " " + e.getSecondEndpoint().getName().toString() + " " + e.getName().toString() + " " + e.getData().toString());
            }
        }*/
		
		System.out.println("********************************");
	}
	
	/*
	 * Find an edge capacity from the graph G by vertex name
	 */
	public Double getEdgeCapacity(String v1, String v2){
		Edge e;
		Iterator i;
		for (i = this.G.edges();i.hasNext();){
			e = (Edge)i.next();
			if(e.getFirstEndpoint().getName().toString().equals(v1) && e.getSecondEndpoint().getName().toString().equals(v2)){
				return (Double)e.getData();
			}
		}
		
		System.out.println("Can't find capacity for this edge:" + v1 + ", " + v2);
		return 0.0;
	}
	
	/*
	 * update the residual graph for the edge v1->v2, with the flow value of (v1,v2) in G
	 */
	public void updateResidualGraph(Vertex v1, Vertex v2, Double flow_value){
		Double capacity = getEdgeCapacity(v1.getName().toString(), v2.getName().toString());
		if(Double.compare(flow_value, capacity) == 0 ){ //f(e) = Ce
			//if there is a forward edge v1-v2 in this.Gf, delete it
			if(findEdge(v1,v2, "forward", this.Gf)){
				removeEdge(v1, v2, "forward", this.Gf);
			}
			else{
				//do nothing
			}
			
			//find the backward edge v2-v1 in Gf, if exist, change its value to flow_value;
			if(findEdge(v2,v1,"backward", this.Gf)){
				updateEdgeData(v2, v1, "backward", flow_value, this.Gf);
			}
			
			//if there is not backward edge v2-v1 in Gf, add a backward edge v2-v1 in Gf
			else
			{
				Vertex v=null,w=null;
				//find v2 vertex in this.Gf
				//find v1 vertex in this.Gf
				Iterator i;
				Vertex currentvertex;
				for(i=this.Gf.vertices();i.hasNext();){
					currentvertex = (Vertex)i.next();
					if( currentvertex.getName().toString().equals(v2.getName().toString()) ){
						v = currentvertex;
					}
					if(currentvertex.getName().toString().equals(v1.getName().toString()) ){
						w = currentvertex;
					}
				}
				//create a new edge with v2,v1, name is "backward"
				Edge newedge = new Edge(v,w,flow_value,"backward");
				//add the new edge to this.Gf
				this.Gf.edgeList.addLast(newedge);
				//add edge to incidentList of v2 in Gf
				v.incidentEdgeList.addLast(newedge);
				//add edge to incidentList of v1 in Gf
				w.incidentEdgeList.addLast(newedge);
			}
		} //f(e)=Ce
		
		else if(Double.compare(flow_value, capacity) < 0){
		//work1: there should be forward edge v1->v2 in Gf with value=Ce-f(e)
			//if there is a forward edge v1-v2 in this.Gf, update its value to C-f(e) in this.Gf
			if(findEdge(v1,v2,"forward",this.Gf))
			{
				updateEdgeData(v1,v2,"forward", (capacity-flow_value),this.Gf);
			}
			//if this is not a forward edge v1-v2 in this.Gf, add it to this.Gf with value C-f(e), ideally this code will not be touched
			else{
				Vertex v=null;
				Vertex w=null;
				Iterator i;
				//find v1 vertex in this.Gf as v
				//find v2 vertex in this.Gf as w
				Vertex currentvertex;
				for(i=this.Gf.vertices();i.hasNext();){
					currentvertex = (Vertex)i.next();
					if(currentvertex.getName().toString().equals(v1.getName().toString()) ){
						v = currentvertex;
					}
					if(currentvertex.getName().toString().equals(v2.getName().toString()) ){
						w = currentvertex;
					}
				}//end find v,w
				//create a new edge with v,w, name is "forward"
				Edge newedge = new Edge(v,w,(capacity-flow_value),"forward");
				//add the new edge to this.Gf
				this.Gf.edgeList.addLast(newedge);
				//add edge to incidentList of v2 in Gf
				v.incidentEdgeList.addLast(newedge);
				//add edge to incidentList of v1 in Gf
				w.incidentEdgeList.addLast(newedge);
			}
		//work2: there should be a backward edge (v2,v1) in Gf, value is flow_value of (v1,v2) in G. 
		//But when f(e) = 0, we should find that backward edge and delete it.
			//if there is a backward edge v2-v1 in this.Gf, update its value to flow_value in this.Gf
			if(findEdge(v2,v1,"backward", this.Gf)){
				if(flow_value==0.0){removeEdge(v2,v1,"backward",this.Gf);}
				else{updateEdgeData(v2,v1,"backward",flow_value,this.Gf);}
			}
			//if there is not a backward edge v2-v1 in this.Gf, add it to this.Gf with value: flow_value only when it is not 0
			else{
				if(flow_value>0){
				Vertex v=null;
				Vertex w=null;
				Iterator i;
				//find v2 vertex in this.Gf as v
				//find v1 vertex in this.Gf as w
				Vertex currentvertex;
				for(i=this.Gf.vertices();i.hasNext();){
					currentvertex = (Vertex)i.next();
					if(currentvertex.getName().toString().equals(v2.getName().toString()) ){
						v = currentvertex;
					}
					if(currentvertex.getName().toString().equals(v1.getName().toString()) ){
						w = currentvertex;
					}
				}//end find v,w
				//create a new edge with v,w, name is "backward"
				Edge newedge = new Edge(v,w,flow_value,"backward");
				//add the new edge to this.Gf
				this.Gf.edgeList.addLast(newedge);
				//add edge to incidentList of v2 in Gf
				v.incidentEdgeList.addLast(newedge);
				//add edge to incidentList of v1 in Gf
				w.incidentEdgeList.addLast(newedge);
				} //add backward edge when the flow value on G is > 0
			}
		} //end f(e) < Ce
			
	}//end updateResidualGraph
	
	
	/*
	 * find an edge v1-v2 from a graph,, return True if it exists.
	 * name can be "forward" or "backward"
	 */
	public boolean findEdge(Vertex v1, Vertex v2, String name, SimpleGraph graph){
		Edge currentedge;
		Iterator i;
		for (i = graph.edges();i.hasNext();){
			currentedge = (Edge)i.next();
			if((currentedge.getFirstEndpoint().getName().toString().equals(v1.getName().toString())) && (currentedge.getSecondEndpoint().getName().toString().equals(v2.getName().toString()) ) && (currentedge.getName().toString().equals(name) ) ){
				return true;
			}
		}
		return false;
	}
	
	/*
	 * remove an edge v1-v2 from the graph
	 */
	public void removeEdge(Vertex v1, Vertex v2, String name, SimpleGraph graph){
		Iterator i;
		Edge currentedge;
		for(i=graph.edges();i.hasNext();){
			currentedge = (Edge)i.next();
			if((currentedge.getFirstEndpoint().getName().toString().equals(v1.getName().toString()) ) && (currentedge.getSecondEndpoint().getName().toString().equals(v2.getName().toString()) ) && (currentedge.getName().toString().equals(name) ) ){
				graph.edgeList.remove(currentedge);  //remove this edge from graph edgelist
				//remove this edge from incident edge for v1
				Iterator j;
				Vertex v;
				for(j=graph.vertices();j.hasNext();){
					v=(Vertex)j.next();
					if(v.getName().toString().equals(v1.getName().toString())){
						v.incidentEdgeList.remove(currentedge);
						break;
					}
				}
				
				//remove this edge from incident edge for v2
				for(j=graph.vertices();j.hasNext();){
					v=(Vertex)j.next();
					if(v.getName().toString().equals(v2.getName().toString())){
						v.incidentEdgeList.remove(currentedge);
						break;
					}
				}
				
				
				return;  //since we've found the edge in graph and removed it, return this method
			}
		}
	}
	
	/*
	 * update the edge value in graph
	 * v: the fristpoint of edge - the start
	 * w: the secondpoint of edge - the end
	 * name: the name of that edge, typlically it is "forward" or "backward"
	 */
	public void updateEdgeData(Vertex v, Vertex w, String name, Double value, SimpleGraph graph){
		Iterator i;
		Edge currentedge;
		for(i=graph.edges();i.hasNext();){
			currentedge = (Edge)i.next();
			if((currentedge.getFirstEndpoint().getName().toString().equals(v.getName().toString()) ) && (currentedge.getSecondEndpoint().getName().toString().equals(w.getName().toString()) ) && (currentedge.getName().toString().equals(name) ) ){
				currentedge.setData(value);
			}
		}
	}
	
	public void updateExcess(String vertexname, double d) {
		//get its previous value
		Double previous_value = this.excess.get(vertexname);
		
		//the new value = previous value + d
		Double new_value = previous_value + d;
		
		this.excess.put(vertexname, new_value);
		
	}
	
	/*
	 * if there is an excess value of any vertex (other than t) greater than 0, return true
	 */
	public boolean excess_greater_than_zero(){
		String key;
		Double value;
		for ( Entry entry : this.excess.entrySet() ){
			key = (String)entry.getKey();
			value = (Double)entry.getValue();
			if(key.equals("t")) {continue;} //we don't count node t
			if(Double.compare(value, 0.0) > 0){
				return true;
			}
		}//end for
		return false;
		
	}
	
	
	 
	/*
	 * find the node with excess at maximum height (other than t)
	 * and e > 0
	 */
	public Vertex getVertexWithMaxExcessValue(){
		Double max_value=0.0;
		String v_name_max=""; //the vertex name which has maximum excess value
		
		String v_name;     
		Double v_excess;
		for(Entry entry : this.excess.entrySet()){  //find the max_value and that vertex name
			v_name = entry.getKey().toString();
			v_excess = (Double)entry.getValue();
			if(v_name.equals("t")) {continue;}  //node t should not be counted
			if(Double.compare(v_excess, max_value)  > 0){
				max_value = v_excess;
				v_name_max = v_name;
			}
		}
		
		Vertex v = null;   //find the vertex by its name "v_name_max"
		Iterator i;
		Vertex currentvertex;
		for(i=this.Gf.vertices();i.hasNext();){
			currentvertex = (Vertex)i.next();
			if(currentvertex.getName().toString().equals(v_name_max)){
				v = currentvertex;
//debug
				//System.out.println("\rfind vertex: " + v.getName() + " in Gf with maximum excess value: " + max_value);
				
				return v;
			}
		} //end for
		
//below code should never be hit
//debug
		//System.out.println("We've found Vertex: " + v_name_max + " with the maximum excess value: " + max_value + " . But we did not find the Vertex from Gf with that name.");
		return null;
		
		

	}
	
	
	/*
	 * find node w that h(w)<h(v), and (v,w) is in Gf
	 * return null if there is not a such node
	 */
	public Vertex getVertexWithLowerHeight(Vertex v){
		int height_v=getHeight(v);;
		int height_w;
		Vertex w;
		
		Iterator i;
		Edge currentedge;
		for(i=v.incidentEdgeList.iterator();i.hasNext();){  //scan v incident edges, find possible w
			currentedge = (Edge)i.next();
			if(currentedge.getFirstEndpoint()==v){ //find edge v->w in Gf
				//get the height of w
				w = currentedge.getSecondEndpoint();
				height_w = getHeight(w);
				//if h(w) < h(v), return w
				if (height_w<height_v){
//debug
					//System.out.println("scan vertex: " + v.getName() + " as v for itsincident edge list in residual graph");
					//System.out.println("w is found for h(w)<h(v) in residual graph");
					//System.out.println("h(v): " + height_v);
					//System.out.println("h(w): " + height_w);
					//System.out.println("v is: " + v.getName());
					//System.out.println("w is: " + w.getName());
					return w;
				}
			}
			else{
				//do nothing, this edge is w->v in Gf, not we need to find, continue the for loop
			}
		} //end for

//debug
		//System.out.println("scan vertex: " + v.getName() + " as v for itsincident edge list in residual graph");
		//System.out.println("there is not a node w found for h(w)<h(v) in residual graph");
		
		return null; //we scan the whole adjacent vertex of v, but none of them has a lower height
		
	}
	
	/*
	 * get any vertex height
	 */
	public int getHeight(Vertex v){
		String key = v.getName().toString();
		return this.height.get(key);
	}
	
	/*
	 * raise v's height
	 */
	public void relabel(Vertex v){
		int v_height = getHeight(v);
		v_height++;
		String key = v.getName().toString();
		this.height.put(key, v_height);
		
		
//debug
		//System.out.println("Relabel, increase height of Vertex (" + v.getName().toString() + ") by 1"); 
	}
	
	/*
	 * push flow from v to w
	 */
	public void push(Vertex v, Vertex w){
//debug
		//System.out.println("push " + v.getName() + ", " + w.getName());
		
		//look up edge type (v,w) in Gf 
		LinkedList v_incidentEdgeList = new LinkedList();  //copy all edges of v to a new list, will scan it
		Iterator j;
		Edge e;
		for(j=v.incidentEdgeList.iterator();j.hasNext();){
			e = (Edge)j.next();
			Edge newedge = new Edge(e.getFirstEndpoint(),e.getSecondEndpoint(),e.getData(),e.getName());
			v_incidentEdgeList.addLast(newedge);
		}
		
		
		Iterator i;
		Edge currentedge;
		for(i=v_incidentEdgeList.iterator();i.hasNext();){
			currentedge = (Edge)i.next();
			
			if((currentedge.getFirstEndpoint()==v)&&(currentedge.getSecondEndpoint()==w)){ //we find an edge v->w in Gf
				//if v->w is a forward edge
				if(currentedge.getName().toString().equals("forward")){
					Double excess_v = getExcess(v);  //the vertex v's excess 
					Double capacity = getEdgeCapacity(v.getName().toString(),w.getName().toString()); //the edge (v,w) capacity in G
					Double flow_value = getFlowValue(v,w); //the current flow value on the edge (v,w) in G
					Double delta = excess_v<(capacity-flow_value)?excess_v:(capacity-flow_value);
					flow_value = flow_value + delta; //increase f(e) by delta
//debug				
					/*
					if(Double.compare(0.0, flow_value)==0){
						System.out.println("possible error");
						System.console().readLine();
					}*/
					
					
					setFlowValue(v,w,flow_value); 
					
					updateResidualGraph(v,w,flow_value);  //since flow value changed on this edge e(v,w), update Gf base on e
					updateExcess(v.getName().toString(),-delta);   //two vertices of this edge should be updated with new excess value
					updateExcess(w.getName().toString(),delta);
					
				}
				
				//if v->w is a backward edge
				if(currentedge.getName().toString().equals("backward")){
					Double excess_v = getExcess(v); //the vertex v's excess
					Double flow_value = getFlowValue(w,v);  //the flow vale on the edge (w,v) in G
					Double delta = excess_v<flow_value?excess_v:flow_value; 
					flow_value = flow_value - delta; //decrease f(e) by delta
//debug				
					/*
					if(Double.compare(0.0, flow_value)==0){
						System.out.println("possible error");
						System.console().readLine();
					}*/
					
					setFlowValue(w,v,flow_value);
					updateResidualGraph(w,v,flow_value);  //since flow value changed on this edge e(v,w), update Gf base on e
					updateExcess(v.getName().toString(),-delta);   //two vertices of this edge should be updated with new excess value
					updateExcess(w.getName().toString(),delta);
				}
			}
			else{
				//do nothing, continue to find the edge (v,w)
			}
		}
		

	}
	
	/*
	 * get excess for any vertex
	 */
	public Double getExcess(Vertex v){
		String key = v.getName().toString();
		return this.excess.get(key);
	}
	
	/*
	 * get the flow value of edge (v,w) in G
	 */
	public Double getFlowValue(Vertex v, Vertex w){
		Iterator i;
		Edge currentedge;
		String v_name = v.getName().toString();
		String w_name = w.getName().toString();
		for(i=this.edgeList_withflow.iterator();i.hasNext();){
			currentedge = (Edge)i.next();
			if(currentedge.getFirstEndpoint().getName().toString().equals(v_name) && currentedge.getSecondEndpoint().getName().toString().equals(w_name)){
				return (Double)currentedge.getData();
			}
		}
		return Double.NEGATIVE_INFINITY;  //never-hit-code
	}
	
	/*
	 * set the flow value on the edge (v,w)
	 */
	public void setFlowValue(Vertex v, Vertex w, Double flow_value){
		Iterator i;
		Edge currentedge;
		String v_name = v.getName().toString();
		String w_name = w.getName().toString();
		for(i=this.edgeList_withflow.iterator();i.hasNext();){
			currentedge = (Edge)i.next();
			if(currentedge.getFirstEndpoint().getName().toString().equals(v_name) && currentedge.getSecondEndpoint().getName().toString().equals(w_name)){
				currentedge.setData(flow_value);
			}
		}
	}
	
	/*
	 * get the sum of flow value for the f(e), e=(v,w) in G
	 */
	public Double getFlowValueOut(String vertex_name){
		Double flow_value = 0.0;
		Iterator i;
		Edge e;
		for(i=this.edgeList_withflow.iterator();i.hasNext();){
			e = (Edge)i.next();
			if(e.getFirstEndpoint().getName().toString().equals(vertex_name)){
				flow_value = flow_value + (Double)e.getData();
			}
		}
		
		return flow_value;
	}
	
	/*
	 * the algorithm
	 */
	public void Preflow_Push_Algorithm(){
		long startTime = System.nanoTime();
		this.init();
		//this.print();
		while(excess_greater_than_zero()){    //stop when no vertex has excess >0 (t is not counted)
			Vertex v  = getVertexWithMaxExcessValue();  //find the node name with excess at maximum height (other than t) in Gf
			
			Vertex w = null;
			w = getVertexWithLowerHeight(v); //find node w that h(w)<h(v), and (v,w) is in Gf. 'null' means for all edges (v,w)in Gf, h(w)>=h(v)
			if(w!=null){
				push(v,w);  //push;
			}
			else{
				relabel(v); //relabel;	
			}
		} //end while
		
		//this.print();
		long endTime = System.nanoTime();
		long runningTime = endTime - startTime;  //running time
		Double flow_value = getFlowValueOut("s");  //the f(v) is the sum of flow value of f(e), e(s,v)
		
		
		System.out.print("The flow value f(v) by Preflow-Push is:           " +  flow_value + ".");
		System.out.println(" Running time is: " + runningTime);

	}
	
	
}
