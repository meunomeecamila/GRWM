package service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.ClassificarRoupasIA;
import model.Prediction;

public class ClassificarRoupaIAService {
	private static final String endpoint_url = "https://grwm-prediction.cognitiveservices.azure.com/customvision/v3.0/Prediction/27c0851e-ff95-45b9-ba34-c525939771fa/classify/iterations/Iteration6/image"; //url IA
	private static final String prediction_key = ""; //chave github impediu
	
	private final HttpClient httpClient = HttpClient.newHttpClient(); //para enviar requisicao
	private final ObjectMapper objectMapper = new ObjectMapper(); //para converter o JSON para objeto em java
	
	/**
     * Envia os bytes da imagem para a API de Previsão da Custom Vision e retorna
     * as tags mais prováveis de cor e tipo
     * @param imageBytes O array de bytes da imagem
     * @return Um Map contendo as chaves "tipo" e "cor" 
     * @throws Exception Se houver falha na comunicação HTTP ou no parse JSON
     */
	public Map<String, String> classificaRoupa(byte[] imageBytes) throws Exception{
		//constroi a requisicao http post
		HttpRequest request  = HttpRequest.newBuilder().uri(URI.create(endpoint_url))
				.header("Prediction-Key", prediction_key)
				.header("Content-Type", "application/octet-stream")
				.POST(HttpRequest.BodyPublishers.ofByteArray(imageBytes)).build();
		
		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		
		//verifica se obteve sucesso
		if(response.statusCode() != 200) {
			throw new IOException("Erro na Custom Vision. Status: " + response.statusCode() + ", Corpo: " + response.body());
		}
		
		ClassificarRoupasIA resultado = objectMapper.readValue(response.body(), ClassificarRoupasIA.class);
		return extractTags(resultado.getPredictions());
	}
	
	/**
     * Analisa a lista de previsoes e extrai
     * o Tipo e a Cor mais provaveis
     * @param predictions A lista de todas as tags previstas pela IA.
     * @return Um mapa com os resultados chave ("tipo" e "cor").
     */
	private Map<String, String> extractTags(List<Prediction> predictions){
		if(predictions == null || predictions.isEmpty()) {
			return Collections.emptyMap(); //se nao ha previsao, retorna vazio
		}
		
		Map<String, String> tags = new HashMap<>();
		
		String melhorTipo = null;
		double maxProbTipo = 0.0;
		
		String melhorCor = null;
		double maxProbCor = 0.0;
		
		//laco para todas as previsoes
		for(Prediction p : predictions) {
			String tagName = p.getTagName();
            double probability = p.getProbability();
            String tagNameLower = tagName.toLowerCase();
            
            //ratreia o melhorCor
            if(tagNameLower.startsWith("cor_")) {
                if (probability > maxProbCor) {
                    maxProbCor = probability;
                    // Remove o prefixo "cor_"
                    melhorCor = tagName.substring(4); 
                }
            }else if (tagNameLower.startsWith("tipo_")) {
                if (probability > maxProbTipo) {
                    maxProbTipo = probability;
                    // Remove o prefixo "tipo_"
                    melhorTipo = tagName.substring(5);
                }
            }
		}
		
		//atribui o tipo e a cor
		if (melhorTipo != null) {
            tags.put("tipo", melhorTipo);
        }
        
        if (melhorCor != null) {
            tags.put("cor", melhorCor);
        }
		
		return tags;
	}
}
