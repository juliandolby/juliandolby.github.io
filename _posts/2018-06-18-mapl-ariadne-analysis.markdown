---
layout: slide
title: "Ariadne: Analysis for Machine Learning"
description: MAPL talk entitled "Analysis for Machine Learning"
date: 2018-06-13 19:45:00 -0500
theme: black
transition: slide
categories: MAPL talk
---

<section style="text-align: center;">
<h3 style="margin-top: 0;">Analysis for Machine Learning</h3>
<br />
<br />
<br />
<p>Julian Dolby, Avraham Shinnar</p>
<p>Allison Allain, Jenna Reinen</p>
<br />
<br />
<p>IBM Thomas J. Watson Research Center</p>
<br />
<br />
<br />
<br />
<p><small>MAPL, PLDI, Philadelphia, June 2018</small></p>
</section>

<section>
<h3>Elucidate Opaque Code</h3>
<br />
<img class="plain" src="/images/before.jpg" border="0" align="center"/>
<div markdown="1">
* ML code manipulates complex, unspecified tensors
</div>
<img class="plain" src="/images/after.jpg" border="0" align="center"/>
<div markdown="1">
* Ariadne tracks and surfaces tensor information
</div>
</section>

<section markdown="1">
<h3>Outline</h3>
<br />

* Describing tensors
* Tensor analysis
* Implementation in WALA
* Initial results
* Future work

</section>

<section markdown="1">
<h3>Tensor Type Example</h3>
<br />
<img class="plain" src="/images/tensortype.jpg" border="0" align="center"/>
* Symbolic dimension for runtime value
* Compound dimension to capture structure
</section>

<section markdown="1">
<h3>Tensor Types</h3>
<br>
$$
\newcommand{\trec}[1]{\{#1\}}
\newcommand{\ttensor}[2]{[{#1} \; \text{of} \; {#2}]}
\newcommand{\ttop}{\top}
    \begin{array}{lrcl}
      (\mbox{Python})
      &\pi& ::= & \trec{f_1 : \pi_1 , \ldots , f_n:\pi_n}\\
      &&\mid&(f_1 : \pi_1, \ldots, f_n:\pi_n)\to\pi\\
      &&\mid& \tau\mid l\mid\ttop\\
      (\mbox{Tensor})&\tau & ::=&\ttensor{d_1 , \ldots , d_n}{\pi}\\
      (\mbox{Dim})&d & ::=& l \mid n \mid l(n) \mid d*d \\
    \end{array}
$$

<br>

* integrate with optional Python Types
* capture structure within dimensions
</section>

<section markdown="1">
<h3>Tensor Analysis</h3>
<br />
* Track tensors through the program
* Make information available everywhere
<img class="plain" src="/images/feeddict.jpg" border="0" align="center"/>
* E.g., clear `feed_dict` has appropriate value for `x`
<br />

</section>

<section markdown="1">
<h3>Tensor Analysis</h3>
<br />
* Sometimes even inputs can be inferred
* `set_shape` imputes tensor shapes to input values
<img class="plain" src="/images/setshape.jpg" border="0" align="center"/>
* Inputs can also be declared explicitly
</section>

<section markdown="1" style="align: center;">
<h3>Tensor Analysis</h3>
<br />
$$T(y) \subseteq \left\{
\begin{array}{ll}
\left\{\mathfrak{I}\right\} & \hbox{$y$ is input} \\
T(x) & y \prec x\\
z & \begin{array}{l}
y \prec {\tt reshape(x, z)} \wedge\\
\exists_{z_i \in S(z)} T(x) \doteq z_i\\
\end{array}\\
\ldots & y \prec \hbox{other Tensorflow APIs}
\end{array}\right. $$

* Input data is specified if necessary
* Tensor operation semantics encoded
* Tensor information follows data flow

</section>

<section markdown="1">
<h3>Implementation in WALA</h3>
<br />
* Exploit WALA support for translating ASTs
* WALA Common AST handles much translation
* Some work remained for idiosyncratic constructs
* Python ASTs created with Jython
* Much shared translation with JavaScript
* Rich source position information vital
* Greatly eases handling myriad popular languages
<br />
</section>

<section markdown="1">
<h3>Simple Translation Example</h3>
<br />
```
public CAstNode visitIf(If arg0) throws Exception {
  return Ast.makeNode(CAstNode.IF_STMT,
    arg0.getInternalTest().accept(this),
    block(arg0.getInternalBody()),
    block(arg0.getInternalOrelse()));
}
```
<br />

* `IF_STMT` provided by WALA
* Jython accessors for node components

</section>

<section markdown="1">
<h3>Evaluated programs</h3>
<br />
* conv_network builds CNN to classify MNIST data.
* mnist_deep classifies with convolutional layers.
* mnist_max simple classifier from Tensorflow.
* mnist_max_xla simple classifier from Tensorflow.
* mnist_sum simple classifier from Tensorflow.
* neuroimage classifies 3D brain images
</section>

<section style="align: center;">
<h3>Tensorflow constructs analyzed</h3>
<br />
${\tiny \begin{array}{|r|l|l|l|l|} \hline
{\rm{program}} & {\rm{reshape}} & {\rm{conv2d}} & {\rm{conv3d}} & {\rm{placeholder}}\\ \hline
conv\_network & &#x2713; & &#x2713; & &#x2717; & &#x2717; \\
mnist\_deep & &#x2713; & &#x2713; & &#x2717; & &#x2713;\\
mnist\_max & &#x2717; & &#x2717; & &#x2717; & &#x2713; \\
mnist\_max\_xla & &#x2717; & &#x2717; & &#x2717; & &#x2713; \\
mnist\_sum & &#x2713; & &#x2717; & &#x2717; &&#x2713; \\
neuroimage & &#x2713; & &#x2717;& &#x2713; & &#x2717;\\ \hline
\end{array}}$
<br />
<br />
<UL>
<LI>no false positives</LI>
<LI>limited modeling so far</LI>
</UL>
</section>

<section markdown="1">
<h3>See our poster</h3>
<br />

<img class="plain" src="/images/maplposter.jpg" width="25%" height="25%" border="0" align="center"/>
<br />

* Poster at MAPL has more on IDE experience

</section>