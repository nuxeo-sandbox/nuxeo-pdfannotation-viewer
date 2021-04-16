## Description
This plugin provides annotation capabilities in the document preview feature.

[![Build Status](https://qa.nuxeo.org/jenkins/buildStatus/icon?job=Sandbox/sandbox_nuxeo-pdfannotation-viewer-master)](https://qa.nuxeo.org/jenkins/job/Sandbox/sandbox_nuxeo-pdfannotation-viewer-master)

## Usage

PDF annotations are available for those users with "Write" access to a document.  There is also an "Annotations" permission that may be assigned to a user to read annotations without having write access to the document.

Use case:

- User1 has Write access and creates several annotations
- User2 has Read & Annotations access to view the annotations
- User3 only has Read access and does not see the annotations

## Requirements
Building requires the following software:
- git
- maven

## How to build
```
git clone https://github.com/nuxeo-sandbox/nuxeo-pdfannotation-viewer
cd nuxeo-pdfannotation-viewer
mvn clean install
```

## Deploying
- Install the marketplace package from the admin center or using nuxeoctl

# Support

**These features are not part of the Nuxeo Production platform.**

These solutions are provided for inspiration and we encourage customers to use them as code samples and learning resources.

This is a moving project (no API maintenance, no deprecation process, etc.) If any of these solutions are found to be useful for the Nuxeo Platform in general, they will be integrated directly into platform, not maintained here.

# License

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)

# About Nuxeo

Nuxeo Platform is an open source Content Services platform, written in Java. Data can be stored in both SQL & NoSQL databases.

The development of the Nuxeo Platform is mostly done by Nuxeo employees with an open development model.

The source code, documentation, roadmap, issue tracker, testing, benchmarks are all public.

Typically, Nuxeo users build different types of information management solutions for [document management](https://www.nuxeo.com/solutions/document-management/), [case management](https://www.nuxeo.com/solutions/case-management/), and [digital asset management](https://www.nuxeo.com/solutions/dam-digital-asset-management/), use cases. It uses schema-flexible metadata & content models that allows content to be repurposed to fulfill future use cases.

More information is available at [www.nuxeo.com](https://www.nuxeo.com).
