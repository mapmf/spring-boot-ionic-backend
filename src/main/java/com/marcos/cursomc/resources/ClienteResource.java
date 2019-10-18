package com.marcos.cursomc.resources;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.marcos.cursomc.domain.Cliente;
import com.marcos.cursomc.services.ClienteService;

@RestController
@RequestMapping("/clientes")
public class ClienteResource {

	@Autowired
	ClienteService clienteService;
	
	@GetMapping("/{id}")
	public ResponseEntity<?> buscar(@PathVariable("id") Integer id){
		
		Cliente cliente = clienteService.buscar(id);
		
		return ResponseEntity.ok(cliente);
	}
}
