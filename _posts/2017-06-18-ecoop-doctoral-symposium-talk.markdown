---
layout: slide
title: "The Story of WALA"
description: ECOOP Doctoral Symposium talk titled "The Story of WALA"
date: 2017-06-18 12:45:00 -0500
theme: black
transition: slide
categories: ECOOP Doctoral Symposium talk
---


<section style="text-align: center;">
<h2 style="margin-top: 0;">The Story of WALA</h2>
<br />
<br />
<br />
<p>Julian Dolby</p>
<p>IBM Thomas J. Watson Research Center</p>
<br />
<br />
<br />
<br />
<p><small>Doctoral Symposium, ECOOP, Barcelona, June 2017</small></p>
</section>

<section markdown="1">

## WALA as Life at IBM ##

* Blend of industry and academia

  * Focus on research and publication

  * Focus on product deliverables

* Balancing act

  * Maintain core research focus

  * Follow corporate trends

* Synergy

  * Find research topics in real problems

  * Apply research results to real problems

</section>

<section markdown="1" style="font-size: 80%;">
<h2 style="font-size: 125%">Selected History</h2>

|-----:|:----:|:------------------------------|
| 2004 | ASTk | J2EE optimization |
|======+======+===============================|
| 2005 | ITM  | Tivoli JavaScript to DB queries |
|======+======+===============================|
| 2006 |      | **Open source release** |
|======+======+===============================|
| 2008 | GBS | ABAP analysis + tooling |
|======+======+===============================|
| 2010 | AppScan | JSA security analysis |
|======+======+===============================|
| 2012 | ECOOP | Correlation tracking |
|======+======+===============================|
| 2013 | ICSE | Approximate call graphs |
|======+======+===============================|
| 2013 |      | Android analysis support
|======+======+===============================|
| 2014 | AppScan | Approximate call graphs in AppScan |
|======+======+===============================|
| 2016 | ICSE+API Harmony | Web API bug detection |
|======+======+===============================|
| 2017 | WCS  | Dialog bug finding analysis |
|======+======+===============================|

</section>

<section markdown="1">
<h2>Enterprise Traditions</h2>

|-----:|:----:|:------------------------------|
| 2004 | ASTk | J2EE optimization |
|======+======+===============================|
| 2005 | ITM  | Tivoli JavaScript to DB queries |
|======+======+===============================|


---------------

* WebSphere J2EE focus for enterprise customers

  * Access relational databases from Java Web server

  * Program analysis to determine read-only aspects

* IBM Tivoli Monitoring migration support

  * Monitoring scripts written in JavaScript

  * Program analysis of monitoring semantics
  
</section>

<section markdown="1">
<h2>Enterprise Traditions</h2>

* WALA was born analyzing J2EE applications

  * Required flexibility to model J2EE semantics

  * Needed to handle large applications

* JavaScript support created for Tivoli

  * Adapt WALA for Tivoli analysis support

  * Created general framework, not one-off hack

</section>

<section markdown="1">
<h3>Open Source Release</h3>


|-----:|:----:|:------------------------------|
| 2006 |      | **Open source release** |
|======+======+===============================|
| 2013 |      | Android analysis support
|======+======+===============================|


------------------

* WALA used in collaborations prior to release

  * e.g. Refinement pointer analysis

  * paperwork for each project

* WALA open source in 2006

  * Encourage collaboration and outside users

  * Manage IP using Eclipse license

</section>

<section markdown="1">
<h3>Open Source Release</h3>


* IBM has embraced open source

* WALA enhanced by contributions

  * numerous fixes and code improvements
  
  * Java source language front end

  * Android application analysis support

  * nodejs analysis support

</section>

<section markdown="1">
<h2>Open Source Release</h2>

* Encourage contributions to WALA

  * Encourage code improvement contributions
  
  * Encourage research with product infrastructure

* Raise awareness of program analysis at Watson

  * Recruiting and interns

  * Release code for publications

</section>

<section markdown="1">
<h2>The Service Economy</h2>

|-----:|:----:|:------------------------------|
| 2008 | GBS  | ABAP analysis + tooling       |


------

* ABAP powers a lot of business software

  * Scripting language for dominant SAP products

  * IBM business upgrading ABAP applications

* Exploit front end from JavaScript to do ABAP too

  * Create ABAP grammar with ANTLR

  * Build simple WALA IR for ABAP constructs

</section>

<section markdown="1">
<h2>JavaScript Security</h2>

|-----:|:----:|:------------------------------|
| 2010 | AppScan | JSA security analysis |
|======+======+===============================|
| 2012 | ECOOP | Correlation tracking |
|======+======+===============================|
| 2013 | ICSE | Approximate call graphs |
|======+======+===============================|
| 2014 | AppScan | <span style="font-size: 80%">Approximate call graphs in AppScan</span> |
|======+======+===============================|


-----

* WALA JavaScript analysis used for security

  * WALA analysis used in AppScan products
  
  * Shipped series of research results in AppScan

</section>

<section markdown="1">
<h2>JavaScript Security</h2>

* Problems first observed by customers

  * Scalability issues in framework code

  * Problem narrowed by product developers

* Anderson's asymptotic complexity worse

  * ECOOP solution: apply context sensitivity

  * ICSE solution: abstraction to avoid issue

</section>

<section markdown="1">
<h2>JavaScript Security</h2>


* Analysis imprecise in common for..in loops
~~~~~~~~
for(prop in source) {
  target[prop] = source[prop];
}
~~~~~~~~
* Flow-insensitive analysis
~~~~~~~~
target[?] = source[?];
~~~~~~~~
* Correlation tracking
~~~~~~~~
if (p === "x") {
  target["x"] = source["x"];
} if (p === "y") {
  target["y"] = source["y"];
...
~~~~~~~~

</section>

<section markdown="1">
<h2>Keeping up with the times</h2>

|-----:|:----:|:------------------------------|
| 2016 | <span style="font-size: 80%;">ICSE+API Harmony</span> | Web API bug detection |
|======+======+===============================|
| 2017 | WCS  | Dialog bug finding analysis |
|======+======+===============================|


-------


* <a href="http://www.ibm.com">ibm.com</a> is all about AI and cognitive computing

  * Machine learning and "Watson" as a product
  
  * People still write code, and hence bugs

  * WALA technology applicable in new domains

</section>

<section markdown="1">
<h2>API Harmony bug finding</h2>
<img class="plain" border="0" align="center" src="/images/pa_moti_code.jpg" width="110%"/>
</section>

<section markdown="1">
<h3>WALA WCS Demo</h3>

<img src="/images/WCS-Screen-Shot.png"/>

</section>

<section markdown="1">
<h3>WALA WCS Demo</h3>

* Watson Conversations dialogs have local variables

<iframe src="/wcsdemo/index.html" frameborder="0" height="1000" width="3500"></iframe>

</section>

<section markdown="1">

## Conclusions ##

* Blend of industry and academia

  * Products drive research and vice versa

* Balancing act

  * WALA has been able to be useful as IBM changes

* Synergy

  * Motivate research topics by real problems

</section>
