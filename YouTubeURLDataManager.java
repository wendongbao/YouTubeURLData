/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package youtubeurldata;

/**
 *
 * @author somak
 */
import com.google.api.services.youtube.YouTube;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.http.HttpRequest;
import com.google.api.services.youtube.YouTube.Videos.List;
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.api.services.youtube.YouTubeRequestInitializer;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.SearchResultSnippet;
import com.google.api.services.youtube.model.ResourceId;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
public class YouTubeURLDataManager {
    
    private YouTube youtube;
    private String userYouTubeURL;
    private String userVideoId;
    private SearchListResponse lastRelatedVideoSearchResponse;
    private String lastRelatedVideoId;
    
    YouTubeURLDataManager(){
        
        youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(),
                new HttpRequestInitializer() {
                    public void initialize(HttpRequest request)
                            throws IOException {
                    }
                })
                .setApplicationName("youTubeURLfinder")
                .setYouTubeRequestInitializer(new YouTubeRequestInitializer("AIzaSyDSPuYKyzystDdUt-aMMpUyKtE4G-5-CjM")).build();
        

                        
    }
    
    public boolean inputURL(String url){
        if (url == null || url.length() == 0){
            return false;
        }
        String videoId = getVideoIDFromURL(url);
        if (videoId == null){
            return false;
        }
        userYouTubeURL = url;
        userVideoId = videoId;
        return true;
        
    }
    
    public void printVideoData(){
        
        printVideoDataFromVideoId(userVideoId);
    }
    
    
    public boolean getRelatedVideos(){
        
        return getRelatedVideosFromVideoId(userVideoId);  
                      
    }
    
    public boolean printVideoDatafromRelatedVideoOption(int option){
        if (lastRelatedVideoSearchResponse == null || 
            lastRelatedVideoSearchResponse.isEmpty()){
            return false;
        }
        if (option < 1 || option > lastRelatedVideoSearchResponse.size()){
            return false;
        }
         
        java.util.List <SearchResult> searchList = lastRelatedVideoSearchResponse.getItems();
        SearchResult requiredResult = searchList.get(option - 1);
        ResourceId resourceId = requiredResult.getId();
        String videoId = resourceId.getVideoId();
        lastRelatedVideoId = videoId;
        printVideoDataFromVideoId(videoId);
        return true;
    }
    
    public boolean getRelatedVideoFromLastRelatedVideoChoice(){
        return getRelatedVideosFromVideoId(lastRelatedVideoId);
    }
    
    private void printVideoDataFromVideoId(String videoId){
        List videosListByIdRequest;
        try {
            videosListByIdRequest = youtube.videos().list("snippet,contentDetails,statistics,player");
            videosListByIdRequest.setId(videoId);
            VideoListResponse response = videosListByIdRequest.execute();
            printResponse(response);
        } catch (IOException ex) {
            System.out.println("Exception in getting vidoes" + ex.toString());
        }
    }
    private boolean getRelatedVideosFromVideoId(String videoId){
        if (videoId == null || videoId.length() == 0){
            return false;
        }
        try {
            YouTube.Search.List searchListByIdRequest = youtube.search().list("snippet");
            searchListByIdRequest.setRelatedToVideoId(videoId);
            searchListByIdRequest.setType("video");
            SearchListResponse response = searchListByIdRequest.execute();
            lastRelatedVideoSearchResponse = response;
            System.out.println("Related videos are :");
            
            java.util.List <SearchResult> searchList = response.getItems();
            
            int i = 1;
            for (SearchResult result: searchList){
                SearchResultSnippet snippet = result.getSnippet();
                System.out.println(i + "." + snippet.getTitle());
                i++;
                
            }
                
        } catch (IOException ex) {
            System.out.println("Exception in getting related vidoes" + ex.toString());
            return false;
        }
        return true;
    }
    private String getVideoIDFromURL(String url){
        String reg = "(?:youtube(?:-nocookie)?\\.com\\/(?:[^\\/\\n\\s]+\\/\\S+\\/|(?:v|e(?:mbed)?)\\/|\\S*?[?&]v=)|youtu\\.be\\/)([a-zA-Z0-9_-]{11})";
        Pattern pattern = Pattern.compile(reg, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(url);

        if (matcher.find())
            return matcher.group(1);
        return null;
    }
    
    private void printResponse(VideoListResponse response){
        if (response.getItems().size() == 0){
            System.out.println("No videos found with this id!");
            return;
        }
        java.util.List <Video> videoList = response.getItems();
        for (Video video: videoList){
            System.out.println("Video id = " + video.getId());
            System.out.println("Video content details = " + video.getContentDetails());
            System.out.println("Video Snippet = " + video.getSnippet());
            System.out.println("Video Statistics = " + video.getStatistics());
            System.out.println("Video Player Details = " + video.getPlayer());
            
        }
    }
}
