package com.marcos.cursomc.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.marcos.cursomc.domain.Categoria;
import com.marcos.cursomc.dto.CategoriaDTO;
import com.marcos.cursomc.repositories.CategoriaRepository;
import com.marcos.cursomc.services.exceptions.DataIntegrityException;
import com.marcos.cursomc.services.exceptions.ObjectNotFoundException;

@Service
public class CategoriaService {

	@Autowired
	private CategoriaRepository repo;

	public Categoria find(Integer id) {

		return repo.findById(id)
				.orElseThrow(() -> new ObjectNotFoundException
						("Categoria com id " + id + " não foi encontrada"
								+ ", Tipo: " + Categoria.class.getName()));
	}

	public Categoria insert(Categoria obj) {
		
		obj.setId(null);
		
		return repo.save(obj);
	}

	public Categoria update(Categoria obj) {

		Categoria newCategoria = find(obj.getId());
		
		updateData(newCategoria, obj);
		
		return repo.save(obj);
	}

	public void delete(Integer id) {

		find(id);

		try {
			
			repo.deleteById(id);
			
		} catch(DataIntegrityViolationException e) {
			
			throw new DataIntegrityException("Não é possível excluir uma categoria que possui produtos");
		}
	}

	public List<Categoria> findAll() {
		return repo.findAll();
	}
	
	public Page<Categoria> findPage(Integer page, Integer linesPerPage, String orderBy, String direction){
		
		PageRequest pageRequest = PageRequest.of(page, linesPerPage, Direction.valueOf(direction), orderBy);
		
		return repo.findAll(pageRequest);
		
	}
	
	public Categoria fromDTO(CategoriaDTO dto) {
		
		return new Categoria(dto.getId(), dto.getNome());
		
	}
	
	private void updateData(Categoria newCategoria, Categoria obj) {

		newCategoria.setNome(obj.getNome());
	}

}
