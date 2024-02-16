
package com.crio.warmup.stock.portfolio;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {




  private RestTemplate restTemplate;


  // Caution: Do not delete or modify the constructor, or else your build will break!
  // This is absolutely necessary for backward compatibility
  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }


  //TODO: CRIO_TASK_MODULE_REFACTOR
  // 1. Now we want to convert our code into a module, so we will not call it from main anymore.
  //    Copy your code from Module#3 PortfolioManagerApplication#calculateAnnualizedReturn
  //    into #calculateAnnualizedReturn function here and ensure it follows the method signature.
  // 2. Logic to read Json file and convert them into Objects will not be required further as our
  //    clients will take care of it, going forward.

  // Note:
  // Make sure to exercise the tests inside PortfolioManagerTest using command below:
  // ./gradlew test --tests PortfolioManagerTest

  //CHECKSTYLE:OFF






  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }

  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Extract the logic to call Tiingo third-party APIs to a separate function.
  //  Remember to fill out the buildUri function and use that.


  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws Exception {

        String tiingoRestURL = buildUri(symbol, from, to);
        TiingoCandle[] tiingoCandleArray =
        restTemplate.getForObject(tiingoRestURL, TiingoCandle[].class);
        if (tiingoCandleArray == null) return new ArrayList<>();
        //return Arrays.stream(tiingoCandleArray).collect(Collectors.toList());
        return Arrays.asList(tiingoCandleArray);
        
  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
       String token = "869d827365d23c93975779388fb04cbb5341da5c";
       //String token = "bfa8811da11126ba8302e83dd0f80a8918d9021d";
       String uriTemplate = "https://api.tiingo.com/tiingo/daily/"+symbol+"/prices?startDate="+startDate.toString()+"&endDate="+endDate.toString()+"&token="+token; 
       return uriTemplate;
  }


  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades,
      LocalDate endDate) throws Exception {
        List<AnnualizedReturn> list = new ArrayList<>();

        for(PortfolioTrade trade:portfolioTrades){

          List<Candle> candlelist = getStockQuote(trade.getSymbol(),trade.getPurchaseDate(), endDate);
          //List<Candle> candlelist = fetchCandles(buildUri(trade.getSymbol(), trade.getPurchaseDate(), endDate));
          //fetching using fetchCandles will lead to UnnecessaryStubbing Exception

          list.add(calculateAnnualizedReturns(endDate,trade,getOpeningPriceOnStartDate(candlelist),getClosingPriceOnEndDate(candlelist)));
        }

        Collections.sort(list,AnnualizedReturn.closingComparator);

        return list;
  }


  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,
      PortfolioTrade trade, Double buyPrice, Double sellPrice) {
        Double totalReturns = (sellPrice-buyPrice)/buyPrice;
        LocalDate purchaseDate = trade.getPurchaseDate();
        Double total_num_years = (double)ChronoUnit.DAYS.between(purchaseDate, endDate)/365;
        Double annualizedReturn = Math.pow(1 + totalReturns, 1.0 / total_num_years) - 1;

      return new AnnualizedReturn(trade.getSymbol(), annualizedReturn, totalReturns);
  }

  public static Double getOpeningPriceOnStartDate(List<Candle> candles) {
    return candles.get(0).getOpen();
 }


 public static Double getClosingPriceOnEndDate(List<Candle> candles) {
   return candles.get(candles.size()-1).getClose();
 }


 public static List<Candle> fetchCandles(String url) {
   
   RestTemplate restTemplate = new RestTemplate();
   TiingoCandle[] templist = restTemplate.getForObject(url,TiingoCandle[].class);
   if(templist==null) return new ArrayList<>();
   List<Candle> list = Arrays.asList(templist);
   return list;
 }



}
