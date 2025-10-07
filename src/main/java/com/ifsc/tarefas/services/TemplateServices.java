package com.ifsc.tarefas.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ifsc.tarefas.model.Prioridade;
import com.ifsc.tarefas.model.Status;
import com.ifsc.tarefas.model.Tarefa;
import com.ifsc.tarefas.auth.RequestAuth;
import com.ifsc.tarefas.model.Categoria;
import com.ifsc.tarefas.repository.CategoriaRepository;
import com.ifsc.tarefas.repository.TarefaRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/templates")
public class TemplateServices 
{
    private final TarefaRepository tarefaRepository;
    private final CategoriaRepository categoriaRepository;

    public TemplateServices(TarefaRepository tarefaRepository, CategoriaRepository categoriaRepository) 
    {
        this.tarefaRepository = tarefaRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @GetMapping("/listar_tarefa")
    String listarTarefas
    (
        Model model, 
        @RequestParam(required = false) String titulo,
        @RequestParam(required = false) String responsavel, 
        @RequestParam(required = false) Status status,
        @RequestParam(required = false) Prioridade prioridade, 
        HttpServletRequest req
    ) 
    {
        String user = RequestAuth.getUser(req);
        String role = RequestAuth.getRole(req);
            
        var tarefas = role.equals("ADMIN") ? 
            tarefaRepository.findAll() :
            tarefaRepository.findByResponsavel(user);

        if (titulo != null && !titulo.trim().isEmpty()) 
        {
            tarefas = tarefas.stream().filter(t -> t.getTitulo().toLowerCase().contains(titulo.toLowerCase())).toList();
        }

        if (responsavel != null && !responsavel.trim().isEmpty()) 
        {
            tarefas = tarefas.stream().filter
            (
                t -> t.getResponsavel().toLowerCase().contains
                (
                    responsavel.toLowerCase()
                )
            ).toList();
        }

        if (status != null) 
        {
            tarefas = tarefas.stream().filter
            (
                t -> t.getStatus() == status
            )
            .collect
            (
                Collectors.toList()
            );
        }

        if (prioridade != null) 
        {
            tarefas = tarefas.stream().filter
            (
                t -> t.getPrioridade() == prioridade
            )
            .collect
            (
                Collectors.toList()
            );
        }

        model.addAttribute("tarefas", tarefas);
        model.addAttribute("listaPrioridade", Prioridade.values());
        model.addAttribute("listaStatus", Status.values());

        model.addAttribute("titulo", titulo);
        model.addAttribute("status", status);
        model.addAttribute("responsavel", responsavel);
        model.addAttribute("prioridade", prioridade);

        return "listar_tarefa";
    }

    @GetMapping("/listar_categoria")
    String listarCategorias
    (
        Model model, 
        @RequestParam(required = false) Long id,
        @RequestParam(required = false) String nome,  
        HttpServletRequest req
    ) 
    {            
        var categorias = categoriaRepository.findAll();

        if (id != null) 
        {
            categorias = categorias.stream().filter(t -> t.getId() == id).toList();
        }

        if (nome != null && !nome.trim().isEmpty()) 
        {
            categorias = categorias.stream().filter
            (
                t -> t.getNome().toLowerCase().contains
                (
                    nome.toLowerCase()
                )
            ).toList();
        }

        model.addAttribute("categoria", categorias);
        model.addAttribute("id", id);
        model.addAttribute("nome", nome);

        return "listar_categoria";
    }    

    @GetMapping("/nova_tarefa")
    String novaTarefa(Model model) 
    {
        model.addAttribute("tarefa", new Tarefa());
        model.addAttribute("prioridades", Prioridade.values());
        model.addAttribute("listaStatus", Status.values());
        
        return "nova_tarefa";
    }

    @GetMapping("/nova_categoria")
    String novaCategoria(Model model) 
    {
        model.addAttribute("categoria", new Categoria());
        
        return "nova_categoria";
    }

    @PostMapping("/salvar_tarefa")
    String salvarTarefa
    (
        @Valid @ModelAttribute("tarefa") Tarefa tarefa, 
        BindingResult br, 
        Model model,
        RedirectAttributes ra
    ) 
    {
        if (br.hasErrors()) 
        {
            model.addAttribute("tarefa", tarefa);
            model.addAttribute("prioridades", Prioridade.values());
            model.addAttribute("listaStatus", Status.values());
            model.addAttribute("erros", "Erro ao salvar tarefa, preencha os campos corretamente.");
            return "nova_tarefa";
        }

        ra.addFlashAttribute("sucesso", "Tarefa salva com sucesso!");

        tarefaRepository.save(tarefa);
        return "redirect:/templates/listar_tarefa";
    }

    @PostMapping("/salvar_categoria")
    String salvarCategoria
    (
        @Valid @ModelAttribute("categoria") Categoria categoria, 
        BindingResult br, 
        Model model,
        RedirectAttributes ra
    ) 
    {
        if (br.hasErrors()) 
        {
            model.addAttribute("categoria", categoria);
            model.addAttribute("erros", "Erro ao salvar categoria, preencha os campos corretamente.");

            return "nova_categoria";
        }

        ra.addFlashAttribute("sucesso", "Categoria salva com sucesso!");

        categoriaRepository.save(categoria);

        return "redirect:/templates/listar_categoria";
    }

    @PostMapping("tarefa/{id}/excluir")
    String excluirTarefa(@PathVariable Long id) 
    {
        tarefaRepository.deleteById(id);
        return "redirect:/templates/listar_tarefa";
    }

    @PostMapping("categoria/{id}/excluir")
    String excluirCategoria(@PathVariable Long id) 
    {
        categoriaRepository.deleteById(id);
        return "redirect:/templates/listar_categoria";
    }

    @GetMapping("tarefa/{id}/editar")
    String editarTarefa(@PathVariable Long id, Model model) 
    {
        var tarefa = tarefaRepository.findById(id).orElse(null);
        if (tarefa == null) 
        {
            return "redirect:/templates/listar_tarefa";
        }
        model.addAttribute("tarefa", tarefa);
        model.addAttribute("prioridades", Prioridade.values());
        model.addAttribute("listaStatus", Status.values());

        return "nova_tarefa";
    }

    @GetMapping("categoria/{id}/editar")
    String editarCategoria(@PathVariable Long id, Model model) 
    {
        var categoria = categoriaRepository.findById(id).orElse(null);
        if (categoria == null) 
        {
            return "redirect:/templates/listar_categoria";
        }
        model.addAttribute("categoria", categoria);
        
        return "nova_categoria";
    }

    @GetMapping("/{tarefaId}/associar_categoria")
    String associarTarefaParaUmaCategoria(Model model, @PathVariable Long tarefaId) 
    {
        List<Categoria> categorias = categoriaRepository.findAll();
        model.addAttribute("categorias", categorias);
        for (Categoria categoria : categorias)
        {
            System.out.println("todas as categorias " + categoria.getNome() + " - " + categoria.getId());
        }
        var tarefa = tarefaRepository.findById(tarefaId);
        model.addAttribute("tarefa", tarefa.get());

        return "gerenciar-categoria";
    }

    @GetMapping("/{tarefaId}/associar_categoria_tarefa")
    String listaCategoriaParaUmaTarefa(Model model, @PathVariable Long tarefaId) 
    {
        var tarefa = tarefaRepository.findById(tarefaId);
        var categorias = categoriaRepository.findAll();

        if (tarefa.isEmpty() || categorias.isEmpty()) 
        {
            return "redirect:/templates/listar_tarefa";
        }

        model.addAttribute("tarefa", tarefa.get());
        model.addAttribute("categorias", categorias);

        return "associar_categoria_tarefa";
    }

    @PostMapping("/{tarefaId}/associar_categoria_tarefa/{categoriaId}")
    public String associarCategoriaParaUmaTarefa(@PathVariable Long tarefaId, @PathVariable Long categoriaId) 
    {
        var tarefa = tarefaRepository.findById(tarefaId);
        var categoria = categoriaRepository.findById(categoriaId);

        if (tarefa.isEmpty() || categoria.isEmpty()) 
        {
            return "redirect:/templates/" + tarefaId + "associar_categoria_tarefa";
        }

        tarefa.get().getCategorias().add(categoria.get());
        tarefaRepository.save(tarefa.get());

        return "redirect:/templates/listar_tarefa";
    }

    @GetMapping("/{id}/tarefas_associadas")
    String listarTarefasAssociadas
    (
        Model model, 
        @PathVariable Long id
    ) 
    {
        var categoria = categoriaRepository.findById(id);
        var listaTarefas = categoria.get().getTarefas();

        model.addAttribute("categoria", categoria.get());
        model.addAttribute("tarefas", listaTarefas);

        return "tarefas_associadas";
    }
}