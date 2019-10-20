# Data-Curation-and-Indexing-with-ElasticSearch

The code creates an index that can support fast search across a corpus consisting a set of legal case reports represented as XML documents. The entities are enriched using Stanford core-nlp to support searches based on specific entity types (e.g.,
person or organization) and then stored data in an ElasticSearch index for fast search.


## **Input**

The input consists in a list of legal case reports represented as XML files. It is taken from UCI Machine Learning Repository. The data can be accessed [here](https://archive.ics.uci.edu/ml/datasets/Legal+Case+Reports). A small excerpt is provided in *cases_test.zip*.


Each XML file follows the schema below:

```
<?xml version="1.0"?>
<case>
   <name>Sharman Networks Ltd v...</name>
   <AustLII>http://www.austlii.edu.au/au/cases...</AustLII>
   <catchphrases>
      <catchphrase>application for leave to appeal...</catchphrase>
      <catchphrase>authorisation of multiple infringements...</catchphrase>
      ...
   <sentences>
      <sentence id="s0">Background to the current application...</sentence>
      <sentence id="s1">1 The applicants Sharman Networks...</sentence>
      ...
   <sentence>
<case/>
```

The schema above shows that a case is made of:
* A name (<name>): This is the title of the case.
* Source URL (<AustLII>): The original source of the legal report.
* A list of catchphrases (<catchphrases>): These are short sentences that summarize the case.
* Sentences (<sentences>): The list of sentences contained in the legal case report.


## **Output**

The final output consists in an ElasticSearch index of legal report cases enriched with the entity type annotations. The resulting index is able to respond to queries (using ElasticSearch’s search APIs) involving the entity types (i.e., person, location, organization) as well as other queries based on general terms (e.g., search for documents that contains the term “criminal law”, search for words appearing in specific properties of the index, etc.).

## **Named Entity Recognition API**

An important part of enriching the legal reports above is the ability to recognize whether a given term is a mention of one of the entity types of interest (i.e., person, location, organization). For example, in the sentence “The person went to the Apple store located in...”, the entity recognition task must be able to identify that the word “Apple” in this sentence refers to an organization (not a fruit). Stanford Core NLP server was used to help recognize named entities.

The server can be downloaded and the full documentation of the tool can be accessed from the link below:
https://stanfordnlp.github.io/CoreNLP/corenlp-server.html

## **Usage**

* Download the *build.sbt* file and complete *src* folder.

* Download and start Stanford core-nlp server on port 9000 (default port if none specified) and an ElasticSearch server on port 9200 (default port if none specified). These links might be helpful-<br/>
[Stanford core-nlp](https://stanfordnlp.github.io/CoreNLP/corenlp-server.html)<br/>
[ElasticSearch](https://www.elastic.co/downloads/elasticsearch)

* The program is built using sbt and run using spark-submit, where the directory containing the legal case report files (XML files) is provided as argument.

* Run the following command on terminal from the directory where build.sbt and /src are stored.<br/>
 ``` $ spark-submit --class "CaseIndex" --master local[2] JAR_FILE FULL_PATH_OF_DIRECTORY_WITH_CASE_FILES ```

* Search the index on terminal using queries like:-<br/>
``` $ curl -X GET "http://localhost:9200/legal_idx/cases/_search?pretty&q=location:Melbourne" ```<br/>
``` $ curl -X GET "http://localhost:9200/legal_idx/cases/_search?pretty&q=person:John" ```<br/>
``` $ curl -X GET "http://localhost:9200/legal_idx/cases/_search?pretty&q=(criminal AND law)" ```

