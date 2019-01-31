## Description
This plugin provides annotation capabilities in the document preview feature.

[![Build Status](https://qa.nuxeo.org/jenkins/buildStatus/icon?job=Sandbox/sandbox_nuxeo-pdfannotation-viewer-master)](https://qa.nuxeo.org/jenkins/job/Sandbox/sandbox_nuxeo-pdfannotation-viewer-master)

## Usage

PDF annotations are available for those users with "Write" access to a document.  There is also an "Annotations" permission that may be assigned to a user to read annotations without having write access to the document.

Use case:

- User1 has Write access and creates several annotations
- User2 has Read & Annotations access to view the annotations
- User3 only has Read access and does not see the annotations

## Notes

As of 10.3, the PDF annotation services will replace all PDF preview portals.  You do not need to enable the viewer for an individual document type.

## Important Note

**These features are not part of the Nuxeo Production platform.**

These solutions are provided for inspiration and we encourage customers to use them as code samples and learning resources.

This is a moving project (no API maintenance, no deprecation process, etc.) If any of these solutions are found to be useful for the Nuxeo Platform in general, they will be integrated directly into platform, not maintained here.

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

## Known limitations
This plugin is a work in progress.

## About Nuxeo
Nuxeo dramatically improves how content-based applications are built, managed and deployed, making customers more agile, innovative and successful. Nuxeo provides a next generation, enterprise ready platform for building traditional and cutting-edge content oriented applications. Combining a powerful application development environment with SaaS-based tools and a modular architecture, the Nuxeo Platform and Products provide clear business value to some of the most recognizable brands including Verizon, Electronic Arts, Netflix, Sharp, FICO, the U.S. Navy, and Boeing. Nuxeo is headquartered in New York and Paris. More information is available at [www.nuxeo.com](http://www.nuxeo.com).
