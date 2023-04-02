import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class App {
    public static void main(String[] args) throws Exception {
        InputStream store = new FileInputStream(args[0]);
        String jsonStoreTxt = IOUtils.toString(store, "UTF-8");
        JSONObject objectStore = new JSONObject(jsonStoreTxt);
        Map<String, LocalTime> pickersMap = new HashMap<>();
        for (Object str : objectStore.getJSONArray("pickers")) {
            for (int i = 0; i < objectStore.getJSONArray("pickers").length(); i++) {
                pickersMap.put(str.toString(),LocalTime.parse(objectStore.getString("pickingStartTime"), DateTimeFormatter.ofPattern("HH:mm")));
            }
        }

        LocalTime localPickingEndTime = LocalTime.parse(objectStore.getString("pickingEndTime"), DateTimeFormatter.ofPattern("HH:mm"));

        InputStream is = new FileInputStream(args[1]);
        String jsonTxt2 = IOUtils.toString(is, "UTF-8");
        JSONArray array = new JSONArray(jsonTxt2);
        Map<String, LocalTime> ordersMapCompletedBy = new LinkedHashMap<>();
        Map<String, Integer> ordersMapPickingTime= new LinkedHashMap<>();
        Map<String, LocalTime> ordersTime= new LinkedHashMap<>();
            for (Object str : array) {
                String[] token = str.toString().split(",");

                String[] orderId = token[0].toString().split("\":\"");
                String order = orderId[1].toString().replace("\"", "");

                String[] pickingTime = token[1].toString().split("\":\"");
                int time;
                try{
                    time = Integer.parseInt(pickingTime[1].toString().replace("PT", "").replace("M\"", ""));
                }catch (NumberFormatException e){
                    time = 0;
                }

                String[] orderValue = token[2].toString().split("\":\"");

                String[] completeBy = token[3].toString().split("\":\"");
                LocalTime completeByTime = LocalTime.parse(completeBy[1].toString().replace("\"}", ""), DateTimeFormatter.ofPattern("HH:mm"));

                ordersMapPickingTime.put(order, time);
                ordersMapCompletedBy.put(order, completeByTime);
                completeByTime=completeByTime.minusMinutes(time);


                ordersTime.put(order,completeByTime);
            }

        Map<String, LocalTime> orderedTime= new LinkedHashMap<>();
        ordersTime.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .forEach(entry -> orderedTime.put(entry.getKey(),entry.getValue()));


        String minPicker = "";
        LocalTime minPickerTime = LocalTime.parse("23:59", DateTimeFormatter.ofPattern("HH:mm"));
        while(!orderedTime.isEmpty() ){
            for(Object picker: pickersMap.keySet()) {

                if(minPickerTime.compareTo(pickersMap.get(picker))==1){
                    minPicker = picker.toString();
                    minPickerTime = pickersMap.get(picker);
                }
            }
            System.out.print(minPicker + " ");
            String key = orderedTime.entrySet().iterator().next().getKey();
            int minutes = ordersMapPickingTime.get(key);
            System.out.print(key+" ");
            System.out.println(minPickerTime);
            minPickerTime = minPickerTime.plusMinutes(minutes);
            pickersMap.put(minPicker,minPickerTime);
            orderedTime.remove(key);
        }
    }
}
