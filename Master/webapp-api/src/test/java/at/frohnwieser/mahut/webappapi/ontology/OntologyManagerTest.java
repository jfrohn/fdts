package at.frohnwieser.mahut.webappapi.ontology;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Ignore;
import org.junit.Test;

public class OntologyManagerTest {

    @Ignore
    @Test
    public void testGetSubclasses() {
	final Collection<String> aList = new ArrayList<String>();
	aList.add("lion");

	OntologyManager.getInstance().getEqualClasses(aList).forEach(System.out::println);
    }
}
