# Introduction #

Our research started from the observation that enterprise-scale software infrastructures fail embarassingly often and take a long time to recover. At the time, about 40% of failures turned out to be due to buggy application software; such failures cost the US economy $60 billion annually (according to NIST). The rate at which developers reduce the number of bugs per line of code (using improved tools, languages and training) was and still is outpaced by the rate at which software grows. The overall number of bugs goes up, and bug-induced system failures continue being a certainty. Conceding that perfect software is just a myth, we focused on ways to recover fast when failures occur.

Our main contributions in this space were _microreboot_ and _macroanalysis_. Microreboot reduces the scope of recovery down to the fine grain of application components; we "reboot" at this fine grain and obtain reduction in recovery time of 1-2 orders of magnitude. Macroanalysis is an application-generic framework for detecting and localizing application-level failures in component-based systems, without requiring a priori knowledge about the application.  Finally, we proposed the notion of _crash-only software_, a design pattern for building systems amenable to microrebooting and macroanalysis; it is centered around fine-grain componentization of systems and separation of application data from application logic.

# Original Team #

  * Prof. [George Candea](http://people.epfl.ch/george.candea) (EPFL)
  * Prof. [Armando Fox](http://www.eecs.berkeley.edu/~fox/) (UC Berkeley)
  * Dr. [Emre Kiciman](http://research.microsoft.com/en-us/people/emrek) (Microsoft Research)
  * Dr. [Ben Ling](http://www.linkedin.com/pub/benjamin-ling/1/405/43a) (Google)

#### Other Contributors ####

  * Mauricio Delgado
  * Greg Friedman
  * Yuichi Fujiki (NEC)
  * Shinichi Kawamoto (Hitachi)
  * Pedram Keyani

# Key Papers #

  * [Autonomous Recovery in Componentized Internet Applications](http://infoscience.epfl.ch/record/98376). George Candea, Emre Kıcıman, Shinichi Kawamoto, and Armando Fox.  _Cluster Computing Journal_, vol. 9, no. 1, February 2006
  * [Microreboot - A Technique for Cheap Recovery](http://infoscience.epfl.ch/getfile.py?recid=97213&mode=best). George Candea, Shinichi Kawamoto, Yuichi Fujiki, Greg Friedman, and Armando Fox. _6th Symposium on Operating Systems Design and Implementation (OSDI)_, December 2004
  * [Detecting Application-Level Failures in Component-based Internet Services](http://research.microsoft.com/apps/pubs/default.aspx?id=75092). Emre Kıcıman and Armando Fox. _IEEE Transactions on Neural Networks_, Special Issue on Adaptive Learning Systems in Communication Networks, September 2005
  * [Path-Based Failure and Evolution Management](http://research.microsoft.com/apps/pubs/default.aspx?id=74738). Mike Y. Chen, Anthony Accardi, Emre Kıcıman, Jim Lloyd, Dave Patterson, Armando Fox, and Eric Brewer. _USENIX/ACM Symposium on Networked Systems Design and Implementation (NSDI)_, March 2004
  * [Session State: Beyond Soft State](http://research.microsoft.com/apps/pubs/default.aspx?id=74713). Benjamin C. Ling, Emre Kıcıman, and Armando Fox. _USENIX/ACM Symposium on Networked Systems Design and Implementation (NSDI)_, March 2004
  * [Recursive Restartability: Turning the Reboot Sledgehammer into a Scalpel](http://infoscience.epfl.ch/record/98456). George Candea and Armando Fox. Workshop on Hot Topics in Operating Systems (HotOS), May 2001.
  * [Crash-Only Software](http://infoscience.epfl.ch/record/98461). George Candea and Armando Fox. _Workshop on Hot Topics in Operating Systems (HotOS)_, May 2003.
  * [Automatic Failure-Path Inference: A Generic Introspection Technique for Internet Applications](http://infoscience.epfl.ch/record/98460). George Candea, Mauricio Delgado, Michael Chen, Armando Fox. _IEEE Workshop on Internet Applications (WIAPP)_, June 2003.
  * [Recovery Oriented Computing: Building Multi-Tier Dependability](http://infoscience.epfl.ch/record/98463). George Candea, Aaron Brown, Armando Fox, and David Patterson. _IEEE Computer_, Vol. 37, No. 11, November 2004


# Resources #

  * Getting started with JAGR ([HTML](GettingStarted_JAGR.md))
  * Getting started with RR-JBoss ([HTML](GettingStarted_RRJBoss.md))
  * Getting started with RR-RUBiS ([HTML](GettingStarted_RRRubis.md))
  * Getting started with RR-SSM ([HTML](GettingStarted_RRSSM.md))
  * Guide to installing RUBiS 1.4.1 on JBoss 3.2.1 ([HTML](Installing_Rubis_on_JBoss.md))
  * Old Petstore 1.3.2 / JBoss 3.2.1 document ([HTML](Installing_Petstore_on_Rubis.md))
  * The EJB Undeploy Mechanism in JBoss ([HTML](Undeploying_EJBs_in_JBoss.md))

If none of these documents help, then consider sending an email to the users mailing list: [jagr-users@googlegroups.com](mailto:jagr-users@googlegroups.com).

# Acknowledgments #

Our project is (and has been) made possible over the years by support from

  * EPFL
  * National Science Foundation (NSF)
  * National Aeronautics and Space Administration (NASA)
  * IBM Research
  * Microsoft Research
  * Stanford Networking Research Center (SNRC)
  * Advanced Computing Systems Association (USENIX)