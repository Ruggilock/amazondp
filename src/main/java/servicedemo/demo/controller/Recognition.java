package servicedemo.demo.controller;


import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.*;
import com.amazonaws.services.s3.model.Bucket;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.springframework.core.SpringVersion;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import servicedemo.demo.amazon.S3aws;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class Recognition {

    @RequestMapping("/recognition")
    public List<Bucket> greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        S3aws s3aws = new S3aws();

        return s3aws.getlistOfBuckets();
    }



    @RequestMapping("/version")
    public String springVersion(@RequestParam(value = "name", defaultValue = "World") String name) {
       return ("version: " + SpringVersion.getVersion());
    }


    @RequestMapping("/compareDate")
    public String compareDate(@RequestParam(value = "name", defaultValue = "World") String name) {
        String dateIni = "06/05/2018";
        String dateCompare = "2018";
        return (dateIni.compareTo(dateCompare) + "");
    }


    @RequestMapping("/sring")
    public String replace(@RequestParam(value = "name", defaultValue = "World") String name) {
        String aRemplazar="https:\\/\\/s3.amazonaws.com\\/dp2awsfacial\\/video_sprite.mp4";

        String remplazado=aRemplazar.replaceAll("\\\\", "");


        return remplazado;
    }

    @RequestMapping("/deletecollection")
    public String deleteCollection(@RequestParam(value = "name", defaultValue = "World") String name) {
        S3aws s3aws = new S3aws();

        String collectionId = s3aws.getNameOfCollection() ;


        DeleteCollectionRequest request = new DeleteCollectionRequest()
                .withCollectionId(collectionId);
        DeleteCollectionResult deleteCollectionResult = s3aws.getRekognitionClient().deleteCollection(request);

        return collectionId + ": " + deleteCollectionResult.getStatusCode()
                .toString();


    }

    @RequestMapping("/getCollections")
    public String listCollection(@RequestParam(value = "name", defaultValue = "World") String name) {
        S3aws s3aws = new S3aws();

        System.out.println("Listing collections");
        int limit = 1;
        ListCollectionsResult listCollectionsResult = null;
        String paginationToken = null;
        do {
            if (listCollectionsResult != null) {
                paginationToken = listCollectionsResult.getNextToken();
            }
            ListCollectionsRequest listCollectionsRequest = new ListCollectionsRequest()
                    .withMaxResults(limit)
                    .withNextToken(paginationToken);
            listCollectionsResult = s3aws.getRekognitionClient().listCollections(listCollectionsRequest);

            List<String> collectionIds = listCollectionsResult.getCollectionIds();
            for (String resultId : collectionIds) {
                System.out.println(resultId);
            }
        } while (listCollectionsResult != null && listCollectionsResult.getNextToken() !=
                null);

        return "";
    }

    @RequestMapping("/listFaces")
    public void listFacesCollection() throws JsonProcessingException {
        S3aws s3aws = new S3aws();

        ObjectMapper objectMapper = new ObjectMapper();
        ListFacesResult listFacesResult = null;

        String paginationToken = null;
        do {
            if (listFacesResult != null) {
                paginationToken = listFacesResult.getNextToken();
            }

            ListFacesRequest listFacesRequest = new ListFacesRequest()
                    .withCollectionId(s3aws.getNameOfCollection())
                    .withMaxResults(1)
                    .withNextToken(paginationToken);

            listFacesResult =  s3aws.getRekognitionClient().listFaces(listFacesRequest);
            List < Face > faces = listFacesResult.getFaces();
            System.out.println(faces.size());
            for (Face face: faces) {
                System.out.println(objectMapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(face));
            }
        } while (listFacesResult != null && listFacesResult.getNextToken() !=
                null);
    }


//    @RequestMapping(value = "/{url}", method = RequestMethod.POST)


    @RequestMapping(value = "/validatePhoto", method = RequestMethod.POST)
    @ResponseBody
    public Boolean validatePhoto(@RequestParam(value = "photo", required = true) String validatePhoto) {
        S3aws s3aws = new S3aws();
        String bucket = s3aws.getBucketName();
        String photo = validatePhoto;
        Boolean response = true;

        DetectFacesRequest request = new DetectFacesRequest()
                .withImage(new Image()
                        .withS3Object(new S3Object()
                                .withName(photo).withBucket(bucket)))
                .withAttributes(Attribute.ALL);


        try {
            DetectFacesResult result = s3aws.getRekognitionClient().detectFaces(request);
            System.out.println("Orientation: " + result.getOrientationCorrection() + "\n");
            List<FaceDetail> faceDetails = result.getFaceDetails();
            if (faceDetails.size() != 1) {
                response = false;
            }

        } catch (AmazonRekognitionException e) {
            e.printStackTrace();
        }
        return response;
    }

    @RequestMapping(value = "/createCollection", method = RequestMethod.POST)
    @ResponseBody
    public String CreateCollectionForFaces() {
        S3aws s3aws = new S3aws();
        String collectionId = s3aws.getNameOfCollection();


        CreateCollectionRequest request = new CreateCollectionRequest()
                .withCollectionId(collectionId);

        CreateCollectionResult createCollectionResult = s3aws.getRekognitionClient().createCollection(request);
        System.out.println("CollectionArn : " +
                createCollectionResult.getCollectionArn());
        System.out.println("Status code : " +
                createCollectionResult.getStatusCode().toString());
        return createCollectionResult.getStatusCode().toString();
    }


    @RequestMapping(value = "/addFaceToCollection", method = RequestMethod.POST)
    @ResponseBody
    public void AddFaceToCollection(@RequestParam(value = "photo", required = true) String namePhoto) {
        S3aws s3aws = new S3aws();
        System.out.println(namePhoto);
        Image image = new Image()
                .withS3Object(new S3Object()
                        .withBucket(s3aws.getBucketName())
                        .withName(namePhoto));

        IndexFacesRequest indexFacesRequest = new IndexFacesRequest()
                .withImage(image)
                .withCollectionId(s3aws.getNameOfCollection())
                .withExternalImageId(namePhoto)
                .withDetectionAttributes("ALL");

        IndexFacesResult indexFacesResult = s3aws.getRekognitionClient().indexFaces(indexFacesRequest);

        System.out.println(namePhoto + " added");
        List<FaceRecord> faceRecords = indexFacesResult.getFaceRecords();
        for (FaceRecord faceRecord : faceRecords) {
            System.out.println("Face detected: Faceid is " +
                    faceRecord.getFace().getFaceId());
        }
    }


    @RequestMapping(value = "/searchFace", method = RequestMethod.POST)
    @ResponseBody
    public String SearchFace(@RequestParam(value = "photo", required = true) String namePhoto) throws Exception {
        S3aws s3aws = new S3aws();

        ObjectMapper objectMapper = new ObjectMapper();

        // Get an image object from S3 bucket.
        Image image = new Image()
                .withS3Object(new S3Object()
                        .withBucket(s3aws.getBucketName())
                        .withName(namePhoto));

        // Search collection for faces similar to the largest face in the image.
        SearchFacesByImageRequest searchFacesByImageRequest = new SearchFacesByImageRequest()
                .withCollectionId(s3aws.getNameOfCollection())
                .withImage(image)
                .withFaceMatchThreshold(95F)
                .withMaxFaces(1);

        SearchFacesByImageResult searchFacesByImageResult =
                s3aws.getRekognitionClient().searchFacesByImage(searchFacesByImageRequest);

        System.out.println("Faces matching largest face in image from" + namePhoto);
        List<FaceMatch> faceImageMatches = searchFacesByImageResult.getFaceMatches();
        if (faceImageMatches.size() > 0) {
            return objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(faceImageMatches.get(0));
        } else return null;

//        for (FaceMatch face: faceImageMatches) {
//            System.out.println(objectMapper.writerWithDefaultPrettyPrinter()
//                    .writeValueAsString(face));
//            System.out.println();
//        }

    }



}