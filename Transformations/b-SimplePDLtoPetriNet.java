package simplepdl.manip;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import simplepdl.Process;
import simplepdl.ResourceSequence;
import simplepdl.WorkDefinition;
import simplepdl.WorkSequence;
import simplepdl.WorkSequenceType;
import simplepdl.SimplepdlFactory;
import simplepdl.SimplepdlPackage;

import petrinet.petriNetwork;
import petrinet.PetriNetElement;
import petrinet.Node;
import petrinet.Transition;
import petrinet.place;
import petrinet.Arc;
import petrinet.ArcType;
import petrinet.PetrinetFactory;
import petrinet.PetrinetPackage;

public class SimplePDLtoPetriNet {

	public static void main(String[] args) {
		
		// Load the SimplePDL package.
		SimplepdlPackage packageInstance = SimplepdlPackage.eINSTANCE;
		
		// Load the PetriNet package.
        PetrinetPackage packagePNInstance = PetrinetPackage.eINSTANCE;
		
		// Load xmi documents.
		Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
		Map<String, Object> m = reg.getExtensionToFactoryMap();
		m.put("xmi", new XMIResourceFactoryImpl());
		
		// Create ResourceSetImpl object.
		ResourceSet resSet = new ResourceSetImpl();
        ResourceSet resSetPetri = new ResourceSetImpl();
        
        // Define resources (models)
        URI modelURI = URI.createURI("models/SimplePDL_Process.xmi");
        URI modelURIPetri = URI.createURI("models/SimplePDL_to_PetriNet.xmi");
        Resource resource = resSet.getResource(modelURI, true);
        Resource resourcePetri = resSetPetri.createResource(modelURIPetri);
		
        // The factory to operate the elements of PetriNet.
        PetrinetFactory myFactory = PetrinetFactory.eINSTANCE;
        
		// Get the first element of the simplePDL model(root).
		Process process = (Process) resource.getContents().get(0);
        
        // Create the first element PetriNet model(root).
        petriNetwork petrinet = myFactory.createpetriNetwork();
        petrinet.setName(process.getName());
      
        // Add the root to the new PetriNet model.
        resourcePetri.getContents().add(petrinet);

        // Create the map to store Nodes in PetriNet.
        HashMap<String, Node> nodes = new HashMap<String, Node>();

        // Convert WorkDefinitions to PetriNet Elements.
        for (Object obj : process.getProcessElements()) {
            if (obj instanceof WorkDefinition){
                WorkDefinition wd = (WorkDefinition) obj;

                // Places
                place place1 = myFactory.createplace();
                String name1 = wd.getName() + "_NotStarted";
                place1.setName(name1);
                place1.setTokenNb(1);;
                petrinet.getElements().add(place1);
                nodes.put(name1, place1);

                place place2 = myFactory.createplace();
                String name2 = wd.getName() + "_Started";
                place2.setName(name2);
                place2.setTokenNb(0);
                petrinet.getElements().add(place2);
                nodes.put(name2, place2);

                place place3 = myFactory.createplace();
                String name3 = wd.getName() + "_InProgress";
                place3.setName(name3);
                place3.setTokenNb(0);
                petrinet.getElements().add(place3);
                nodes.put(name3, place3);

                place place4 = myFactory.createplace();
                String name4 = wd.getName() + "_Finished";
                place4.setName(name4);
                place3.setTokenNb(0);
                petrinet.getElements().add(place4);
                nodes.put(name4,place4);

                // Transitions
                Transition transition1 = myFactory.createTransition();
                String name_tr1 = "Start_" + wd.getName();
                transition1.setName(name_tr1);
                petrinet.getElements().add(transition1);
                nodes.put(name_tr1,transition1);

                Transition transition2 = myFactory.createTransition();
                String name_tr2 = "Finish_" + wd.getName();
                transition2.setName(name_tr2);
                petrinet.getElements().add(transition2);
                nodes.put(name_tr2,transition2);
                
                // Arcs
                Arc arc1 = myFactory.createArc();
                arc1.setSource(place1);
                arc1.setTarget(transition1);             
                arc1.setWeight(1);
                arc1.setName(place1.getName() + " -> " + transition1.getName());
                petrinet.getElements().add(arc1);

                Arc arc2 = myFactory.createArc();
                arc2.setSource(transition1);
                arc2.setTarget(place2);
                arc2.setWeight(1);
                arc2.setName(transition1.getName() + " -> " + place2.getName());
                petrinet.getElements().add(arc2);              

                Arc arc3 = myFactory.createArc();
                arc3.setSource(transition1);
                arc3.setTarget(place3);
                arc3.setWeight(1);
                arc3.setName(transition1.getName() + " -> " + place3.getName());
                petrinet.getElements().add(arc3);

                Arc arc4 = myFactory.createArc();
                arc4.setSource(place3);
                arc4.setTarget(transition2);
                arc4.setWeight(1);
                arc4.setName(place3.getName() + " -> " + transition2.getName());
                petrinet.getElements().add(arc4);

                Arc arc5 = myFactory.createArc();
                arc5.setSource(transition2);
                arc5.setTarget(place4);
                arc5.setWeight(1);
                arc5.setName(transition2.getName() + " -> " + place4.getName());
                petrinet.getElements().add(arc5);      
            }
        }
        
        // Convert WorkSequences into PetriNet Elements
        for (Object obj : process.getProcessElements()){
            if (obj instanceof WorkSequence){
            	
            	// Load info of WorkSequence
                WorkSequence ws = (WorkSequence) obj;
                WorkDefinition wd1 = ws.getPredecessor();
                WorkDefinition wd2 = ws.getSuccessor();
                WorkSequenceType type = ws.getLinkType();
                
                // Convert
                place predecessor;
                Transition successor;
                
                if(type.equals(WorkSequenceType.FINISH_TO_START)) {
					Arc arcf2s = myFactory.createArc();
					arcf2s.setType(ArcType.READ_ARC);
					arcf2s.setWeight(1);
					predecessor = (place) nodes.get(wd1.getName() + "_Finished");
					arcf2s.setSource(predecessor);
					successor = (Transition) nodes.get("Start_" + wd2.getName());
					arcf2s.setTarget(successor);
					arcf2s.setName(predecessor.getName() + " -> " + successor.getName());
					petrinet.getElements().add(arcf2s);
				} else if(type.equals(WorkSequenceType.FINISH_TO_FINISH)) {
					Arc arcf2f = myFactory.createArc();
					arcf2f.setType(ArcType.READ_ARC);
					arcf2f.setWeight(1);
					predecessor = (place) nodes.get(wd1.getName() + "_Finished");
					arcf2f.setSource(predecessor);
					successor = (Transition) nodes.get("Finish_" + wd2.getName());
					arcf2f.setTarget(successor);
					arcf2f.setName(predecessor.getName() + " -> " + successor.getName());
					petrinet.getElements().add(arcf2f);
				} else if(type.equals(WorkSequenceType.START_TO_START)) {
					Arc arcs2s = myFactory.createArc();
					arcs2s.setType(ArcType.READ_ARC);
					arcs2s.setWeight(1);
					predecessor = (place) nodes.get(wd1.getName() + "_Started");
					arcs2s.setSource(predecessor);
					successor = (Transition) nodes.get("Start_" + wd2.getName());
					arcs2s.setTarget(successor);
					arcs2s.setName(predecessor.getName() + " -> " + successor.getName());
					petrinet.getElements().add(arcs2s);
				} else if(type.equals(WorkSequenceType.START_TO_FINISH)) {
					Arc arcs2f = myFactory.createArc();
					arcs2f.setType(ArcType.READ_ARC);
					arcs2f.setWeight(1);
					predecessor = (place) nodes.get(wd1.getName() + "_Started");
					arcs2f.setSource(predecessor);
					successor = (Transition) nodes.get("Finish_" + wd2.getName());
					arcs2f.setTarget(successor);
					arcs2f.setName(predecessor.getName() + " -> " + successor.getName());
					petrinet.getElements().add(arcs2f);
				}
            }
        }

        // Convert Resources to PetriNet Elements
        for (Object obj : process.getProcessElements()){
            if (obj instanceof simplepdl.Resource){
                simplepdl.Resource resources = (simplepdl.Resource) obj;
                
                // place of Resources
                place placeR = myFactory.createplace();
                placeR.setName(resources.getResourcetype().toString());
                placeR.setTokenNb(resources.getNum());
                petrinet.getElements().add(placeR);
                nodes.put(placeR.getName(), placeR);
            }
        }
        
        // Convert ResourceSequence to PetriNet Elements
        for(Object obj: process.getProcessElements()) {
        	if(obj instanceof ResourceSequence) {
        		// Load info of ResourceSequence
        		ResourceSequence rs = (ResourceSequence)obj;
        		simplepdl.Resource placeR = rs.getResource();
        		WorkDefinition wd = rs.getWorkdefinition();
        		int weight = rs.getWeight();
        		
        		// Resource in
        		place predecessorStart;
                Transition successorStart;
                
        		Arc arcStart = myFactory.createArc();
        		arcStart.setType(ArcType.NORMAL);
        		arcStart.setWeight(weight);
        		predecessorStart = (place)nodes.get(placeR.getResourcetype().toString());
        		arcStart.setSource(predecessorStart);
        		successorStart = (Transition)nodes.get("Start_" + wd.getName());
        		arcStart.setTarget(successorStart);
        		arcStart.setName(predecessorStart.getName() + " -> " + successorStart.getName());
        		petrinet.getElements().add(arcStart);
        		
        		
        		// Resource out
        		Transition predecessorFinish;
        		place successorFinish;
        		
        		Arc arcFinish = myFactory.createArc();
        		arcFinish.setType(ArcType.NORMAL);
        		arcFinish.setWeight(weight);
        		predecessorFinish = (Transition)nodes.get("Finish_" + wd.getName());
        		arcFinish.setSource(predecessorFinish);
        		successorFinish = (place)nodes.get(placeR.getResourcetype().toString());
        		arcFinish.setTarget(successorFinish);
        		arcFinish.setName(predecessorFinish.getName() + " -> " + successorFinish.getName());
        		petrinet.getElements().add(arcFinish);       	
        		
        		
        	}
        }
        
    // Save the resource
    try {
    	resourcePetri.save(Collections.EMPTY_MAP);
    } catch (IOException e) {
        e.printStackTrace();
    }
    
    }
}