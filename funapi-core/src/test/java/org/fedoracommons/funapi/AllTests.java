package org.fedoracommons.funapi;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

//import org.fedoracommons.funapi.fedora.FedoraResolverTest;
//import org.fedoracommons.funapi.pmh.dspace.DSpacePmhResolverTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({FormatsSerializerTest.class
//                        , 
//                     FedoraResolverTest.class,
//                     DSpacePmhResolverTest.class
                     })
public class AllTests {
}
