package br.gov.servicos.busca;

import br.gov.servicos.cms.ConteudoRepository;
import br.gov.servicos.servico.Servico;
import br.gov.servicos.servico.ServicoRepository;
import lombok.experimental.FieldDefaults;
import org.elasticsearch.index.query.QueryBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.core.FacetedPageImpl;

import static br.gov.servicos.fixtures.TestData.SERVICO;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static lombok.AccessLevel.PRIVATE;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.ModelAndViewAssert.assertModelAttributeValue;
import static org.springframework.test.web.ModelAndViewAssert.assertViewName;

@RunWith(MockitoJUnitRunner.class)
@FieldDefaults(level = PRIVATE)
public class BuscaControllerTest {

    @Mock
    ServicoRepository servicos;
    @Mock
    ConteudoRepository conteudos;

    @Mock
    Buscador buscador;

    Page<Servico> umServico = new FacetedPageImpl<>(singletonList(SERVICO));
    BuscaController controller;

    @Before
    public void setUp() {
        doReturn(emptyList())
                .when(servicos)
                .search(any(QueryBuilder.class));
        doReturn(emptyList())
                .when(conteudos)
                .search(any(QueryBuilder.class));

        controller = new BuscaController(buscador, conteudos);
    }

    @Test
    public void buscaRedirecionaParaPaginaDeResultados() {
        assertViewName(controller.busca(null, 1), "resultados-busca");
    }

    @Test
    public void buscaRetornaTermoBuscado() {
        assertModelAttributeValue(controller.busca("trabalho", 1), "termo", "trabalho");
    }

    @Test
    public void buscaRetornaResultadosParaAPagina() {
        doReturn(umServico)
                .when(buscador)
                .busca(of("emprego"), 0);

        assertModelAttributeValue(controller.busca("emprego", 1), "resultados", umServico);
    }

    @Test
    public void deveUsarBase0AoBuscarResultadosPaginados() {
        controller.busca("qualquer servico", 20);
        verify(buscador).busca(of("qualquer servico"), 19);
    }

    @Test
    public void retornaSugestoesDeBusca() throws Exception {
        doReturn(singletonList(SERVICO.withTitulo("Seguro-desemprego")))
                .when(buscador)
                .buscaSemelhante(ofNullable("empreg"), "titulo", "descricao");

        String sugestao = controller.sugestao("empreg");

        assertThat(sugestao, is("[\"empreg\", [\"Seguro-desemprego\"]]"));
    }
}
