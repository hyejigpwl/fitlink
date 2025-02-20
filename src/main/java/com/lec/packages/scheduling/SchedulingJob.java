package com.lec.packages.scheduling;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.lec.packages.domain.Facility;
import com.lec.packages.repository.FacilityRepository;
import com.lec.packages.repository.ReservationRepository;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class SchedulingJob {

    @Value("${00Data.admin.memId}")
	private String memId;

    @Value("${00Data.admin.keyString}")
    private String apiKey;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private FacilityRepository facilityRepository;

    @Scheduled(cron = "0 0 12 * * ?")
    @Transactional
    public void removeReservationRecord() {
        LocalDate today = LocalDate.now();
        int updatedCount = reservationRepository.markOldReservationsAsDeleted(today);
 
        if (updatedCount > 0) {
            log.info("✅ " + updatedCount + "개의 예약이 삭제되었습니다.");
        } else {
            log.info("📌 삭제할 예약 없음.");
        }
    }

    @Scheduled(cron = "0 0 12 * * ?")
    @Transactional
    public void getSetFacilityFromAPI() {

        log.info("start batch getsetfacility");
        HttpURLConnection urlConnection = null;
        InputStream stream = null;
        String result = null;

        String urlString = "https://openapi.gg.go.kr/PublicTrainingFacilitySoccer?" 
                            + "KEY=" + apiKey
                            + "&Type=json"
                            + "&pSize=1000";

        try {
            URL url = new URL(urlString);

            urlConnection = (HttpURLConnection) url.openConnection();
            stream = getNetworkConnection(urlConnection);

            result = readStreamToString(stream);
            if(stream != null) stream.close();
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            if(urlConnection != null) urlConnection.disconnect();
            log.info("success get api Data");
        }

        try {
            JSONParser parser = new JSONParser();
            JSONObject object = (JSONObject) parser.parse(result);
            JSONArray publicTrainingFacilitySoccer = (JSONArray) object.get("PublicTrainingFacilitySoccer");
            JSONObject datas = (JSONObject) publicTrainingFacilitySoccer.get(1);
            JSONArray rows = (JSONArray) datas.get("row");

            String description = "공공 시설입니다. 깨끗하게 이용해 주세요";
            LocalTime startTime = LocalTime.of(8, 00);
            LocalTime endTime = LocalTime.of(22, 00);

            List<Facility> facilities = new ArrayList<>();

            for (Object row : rows) {
                JSONObject facilityInfo = (JSONObject) row;
                if(facilityInfo.get("REFINE_ZIP_CD") == null || facilityInfo.get("REFINE_WGS84_LAT") == null || facilityInfo.get("REFINE_WGS84_LOGT") == null) continue;
                BigDecimal latBigDecimal = new BigDecimal(facilityInfo.get("REFINE_WGS84_LAT").toString());
                BigDecimal longtBigDecimal = new BigDecimal(facilityInfo.get("REFINE_WGS84_LOGT").toString());

                log.info(latBigDecimal);
                log.info(longtBigDecimal);

                Facility facility = Facility.builder().deleteFlag(false).exerciseCode("EXE_FOOTBALL").facilityAddress(facilityInfo.get("REFINE_LOTNO_ADDR").toString())
                                                    .facilityCode(createFacilityCode(facilityInfo.get("REFINE_ZIP_CD").toString())).facilityDescription(description)
                                                    .facilityEndTime(endTime).facilityIsOnlyClub(false).facilityName(facilityInfo.get("FACLT_NM").toString())
                                                    .facilityLat(latBigDecimal).facilityLongt(longtBigDecimal).facilityName(facilityInfo.get("FACLT_NM").toString())
                                                    .facilityStartTime(startTime).facilityZipcode(facilityInfo.get("REFINE_ZIP_CD").toString()).memId(memId)
                                                    .price(new BigDecimal(100000)).build();

                facilities.add(facility);
            }

            facilityRepository.saveAll(facilities);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        log.info("end batch getsetfacility");
    }

    private InputStream getNetworkConnection(HttpURLConnection urlConnection) throws IOException{

        urlConnection.setRequestMethod("POST");

        if(urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("OPEN API ERROR" + urlConnection.getResponseCode());
        }

        return urlConnection.getInputStream();
    }

    private String readStreamToString(InputStream stream) throws IOException{
        StringBuilder sbResult = new StringBuilder();

        BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));

        String brString;
        while ((brString = br.readLine()) != null) {
            sbResult.append(brString);
        }

        br.close();

        return sbResult.toString();
    }

    private String createFacilityCode(String zipCode) {
        return "00DATA_" + zipCode;
    }
}
