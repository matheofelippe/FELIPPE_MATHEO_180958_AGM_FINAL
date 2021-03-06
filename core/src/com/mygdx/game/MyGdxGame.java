package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.BitSet;
import java.util.Random;

public class MyGdxGame extends ApplicationAdapter {

	//Declaracao das variaveis das imagens que serao usadas no jogo
	private SpriteBatch batch;
	private Texture[] passaros;
	private Texture fundo;
	private Texture title_screen;
	private Texture canoBaixo;
	private Texture canoTopo;
	private Texture gameOver;
	private Texture moedaDourada;
	private Texture moedaPrata;

	//Declaracao das variaveis colisores do jogo
	private ShapeRenderer shapeRenderer;
	private Circle circuloPassaro;
	private Rectangle retanguloCanoCima;
	private Rectangle retanguloCanoBaixo;
	private Circle circuloMoedaDourada;
	private Circle circuloMoedaPrata;


	//Declaracoes de variaveis que guardar posicoes e dimensoues da tela
	private float larguraDispositivo;
	private float alturaDispositivo;
	private float variacao=0;
	private float gravidade = 2;
	private float posicaoInicialVerticalPassaro=0;
	private float posicaoCanoHorizontal;
	private float posicaoCanoVertical;
	private float espacoEntreCanos;
	private Random random;
	private int pontos = 0;
	private int pontuacaoMaxima = 0;
	private boolean passouCano = false;
	private int estadoJogo=0;
	private float posicaoHorizontalPassaro=0;
	private float posicaoVerticalPassaro=0;
	private float posicaoHorizontalMoedaDourada = 0;
	private float posicaoVerticalMoedaDourada = 0;
	private float posicaoHorizontalMoedaPrata = 0;
	private float posicaoVerticalMoedaPrata = 0;

	//Declaracoes das variavies que exibiram os textos no jogo
	BitmapFont textoPontuacao;
	BitmapFont textoReiniciar;
	BitmapFont textoMelhorPontuacao;

	//Declaracoes das variavies de som
	Sound somVoando;
	Sound somColisao;
	Sound somPontuacao;

	//Declaracao da variavel que guarda as preferencias
	Preferences preferences;

	//Declaracoes das variavies da camera, tela do jogo e dimensoes
	private OrthographicCamera camera;
	private Viewport viewport;
	private final float VIRTUAL_WIDTH = 720;
	private final float VIRTUAL_HEIGHT = 1280;

	@Override
	public void create () {
		inicializarTexturas();
		inicializaObjetos();
	}

	private void inicializaObjetos() {
		batch = new SpriteBatch();
		random = new Random();

		larguraDispositivo = VIRTUAL_WIDTH;
		alturaDispositivo = VIRTUAL_HEIGHT;
		posicaoInicialVerticalPassaro = alturaDispositivo/2;
		posicaoCanoHorizontal = larguraDispositivo;
		espacoEntreCanos = 350;

		posicaoHorizontalMoedaDourada = random.nextInt((int) larguraDispositivo / 2);
		posicaoVerticalMoedaDourada = random.nextInt((int) alturaDispositivo);

		posicaoHorizontalMoedaPrata = random.nextInt((int) larguraDispositivo / 2);
		posicaoVerticalMoedaPrata = random.nextInt((int) alturaDispositivo);


		textoPontuacao = new BitmapFont();
		textoPontuacao.setColor(Color.WHITE);
		textoPontuacao.getData().setScale(10);

		textoReiniciar = new BitmapFont();
		textoReiniciar.setColor(Color.GREEN);
		textoReiniciar.getData().setScale(2);

		textoMelhorPontuacao = new BitmapFont();
		textoMelhorPontuacao.setColor(Color.RED);
		textoMelhorPontuacao.getData().setScale(2);

		shapeRenderer = new ShapeRenderer();
		circuloPassaro = new Circle();
		retanguloCanoBaixo = new Rectangle();
		retanguloCanoCima = new Rectangle();
		circuloMoedaDourada = new Circle();
		circuloMoedaPrata = new Circle();

		somVoando = Gdx.audio.newSound(Gdx.files.internal("som_asa.wav"));
		somColisao = Gdx.audio.newSound(Gdx.files.internal("som_batida.wav"));
		somPontuacao = Gdx.audio.newSound(Gdx.files.internal("som_pontos.wav"));

		preferences = Gdx.app.getPreferences("flabbyBird");
		pontuacaoMaxima = preferences.getInteger("pontuacaoMaxima", 0);

		camera = new OrthographicCamera();
		camera.position.set(VIRTUAL_WIDTH/2, VIRTUAL_HEIGHT/2,0);
		viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
	}

	private void inicializarTexturas() {
		passaros = new Texture[3];
		passaros[0] = new Texture("angrybird_1.png");
		passaros[1] = new Texture("angrybird_2.png");
		passaros[2] = new Texture("angrybird_1.png");

		fundo = new Texture("fundo.png");
		title_screen = new Texture("title_screen.jpg");
		canoBaixo = new Texture("cano_baixo_maior.png");
		canoTopo = new Texture("cano_topo_maior.png");
		gameOver = new Texture("game_over.png");
		moedaPrata = new Texture("silvercoin.png");
		moedaDourada = new Texture("goldcoin.png");
	}

	//
	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		verificarEstadoJogo();
		validarPontos();
		desenharTexturas();
		detectarColisoes();
	}

	private void verificarEstadoJogo() {

		boolean toqueTela = Gdx.input.justTouched();
		if(estadoJogo == 0){
			if(toqueTela){
				gravidade = -15;
				estadoJogo = 1;
				somVoando.play();
			}
		}else if(estadoJogo==1){
			if(toqueTela){
				gravidade = -15;
				somVoando.play();
			}
			posicaoCanoHorizontal -= Gdx.graphics.getDeltaTime() * 200;
			if(posicaoCanoHorizontal < -canoTopo.getWidth()){
				posicaoCanoHorizontal = larguraDispositivo;
				posicaoCanoVertical = random.nextInt(400)-200;
				passouCano = false;
			}
			if(posicaoInicialVerticalPassaro > 0 || toqueTela)
				posicaoInicialVerticalPassaro = posicaoInicialVerticalPassaro - gravidade;
			gravidade++;

			posicaoHorizontalMoedaDourada -= Gdx.graphics.getDeltaTime() * 200;

			if (posicaoHorizontalMoedaDourada <= -moedaDourada.getWidth()){
				posicaoHorizontalMoedaDourada = larguraDispositivo;
				posicaoVerticalMoedaDourada = random.nextInt((int) alturaDispositivo);
			}

			posicaoHorizontalMoedaPrata -= Gdx.graphics.getDeltaTime() * 200;

			if (posicaoHorizontalMoedaPrata <= -moedaPrata.getWidth()){
				posicaoHorizontalMoedaPrata = larguraDispositivo;
				posicaoVerticalMoedaPrata = random.nextInt((int) alturaDispositivo);
			}

		}else if( estadoJogo == 2){
			if(pontos > pontuacaoMaxima){
				pontuacaoMaxima = pontos;
				preferences.putInteger("pontuacaoMaxima", pontuacaoMaxima);
				preferences.flush();
			}
			posicaoCanoHorizontal -= Gdx.graphics.getDeltaTime()*500;
			if(toqueTela){
				estadoJogo = 0;
				pontos = 0;
				gravidade = 0;
				posicaoHorizontalPassaro = 0;
				posicaoInicialVerticalPassaro = alturaDispositivo / 2;
				posicaoCanoHorizontal = larguraDispositivo;
			}
		}

	}

	private void validarPontos() {
		if(posicaoCanoHorizontal< 50-passaros[0].getWidth()){
			if(!passouCano){
				pontos++;
				passouCano = true;
				somPontuacao.play();
			}
		}

		variacao += Gdx.graphics.getDeltaTime() * 10;

		if (variacao > 3){
			variacao = 0;
		}

	}

	private void desenharTexturas() {
		batch.setProjectionMatrix( camera.combined);
		batch.begin();
		batch.draw(fundo,0,0,larguraDispositivo, alturaDispositivo);
		batch.draw(passaros[(int) variacao], 50 + posicaoHorizontalPassaro,posicaoInicialVerticalPassaro);
		batch.draw(canoBaixo,posicaoCanoHorizontal,alturaDispositivo / 2 - canoBaixo.getHeight()-espacoEntreCanos/2+posicaoCanoVertical);
		batch.draw(canoTopo, posicaoCanoHorizontal, alturaDispositivo / 2 + espacoEntreCanos / 2 + posicaoCanoVertical);
		textoPontuacao.draw(batch, String.valueOf(pontos), larguraDispositivo/2,alturaDispositivo-110);
		batch.draw(moedaDourada, posicaoHorizontalMoedaDourada, posicaoVerticalMoedaDourada);
		batch.draw(moedaPrata, posicaoHorizontalMoedaPrata, posicaoVerticalMoedaPrata);

		if(estadoJogo==0){
			batch.draw(title_screen,0,0,larguraDispositivo, alturaDispositivo);
		}

		if( estadoJogo == 2){batch.draw(gameOver, larguraDispositivo / 2 - gameOver.getWidth()/2, alturaDispositivo/2);
			textoReiniciar.draw(batch,"Toque para reiniciar", larguraDispositivo / 2 - 140,alturaDispositivo / 2 - gameOver.getHeight() / 2);
			textoMelhorPontuacao.draw(batch,"Seu recorde ??: "+pontuacaoMaxima+" pontos",larguraDispositivo / 2 - 140, alturaDispositivo / 2 - gameOver.getHeight());
		}
		batch.end();

	}

	private void detectarColisoes() {
		circuloPassaro.set(
				50 + posicaoHorizontalPassaro + passaros[0].getWidth()/2,
				posicaoInicialVerticalPassaro + passaros[0].getHeight()/2,
				passaros[0].getWidth()/2
		);

		retanguloCanoBaixo.set(
				posicaoCanoHorizontal,
				alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos / 2 + posicaoCanoVertical,
				canoBaixo.getWidth(), canoBaixo.getHeight()
		);

		retanguloCanoCima.set(
				posicaoCanoHorizontal, alturaDispositivo / 2 + espacoEntreCanos / 2 + posicaoCanoVertical,
				canoTopo.getWidth(), canoTopo.getHeight()

		);

		circuloMoedaDourada.set(
				posicaoHorizontalMoedaDourada + moedaDourada.getWidth() / 2,
				posicaoVerticalMoedaDourada + moedaDourada.getHeight() / 2,
				moedaDourada.getWidth() / 2
		);

		// Circulo de colisao da moeda prata
		circuloMoedaPrata.set(
				posicaoHorizontalMoedaPrata + moedaPrata.getWidth() / 2,
				posicaoVerticalMoedaPrata + moedaPrata.getHeight() / 2,
				moedaPrata.getWidth() / 2
		);

		boolean colidiuCanoCima = Intersector.overlaps(circuloPassaro, retanguloCanoCima);
		boolean colidiuCanoBaixo = Intersector.overlaps(circuloPassaro, retanguloCanoBaixo);
		boolean colidiuMoedaDourada = Intersector.overlaps(circuloPassaro, circuloMoedaDourada);
		boolean colidiuMoedaPrata = Intersector.overlaps(circuloPassaro, circuloMoedaPrata);

		if(colidiuCanoCima || colidiuCanoBaixo){
			if(estadoJogo == 1){
				somColisao.play();
				estadoJogo = 2;
			}
		}

		if (colidiuMoedaPrata){
			pontos += 5;
			somPontuacao.play();

			posicaoHorizontalMoedaPrata = larguraDispositivo;
			posicaoVerticalMoedaPrata = random.nextInt((int) alturaDispositivo);
		}

		if (colidiuMoedaDourada){
			pontos += 10;
			somPontuacao.play();

			posicaoHorizontalMoedaDourada = larguraDispositivo;
			posicaoVerticalMoedaDourada = random.nextInt((int) alturaDispositivo);
		}

	}



	@Override
	public void resize(int width, int height){
		viewport.update(width, height);
	}

	@Override
	public void dispose () {
	}
}
