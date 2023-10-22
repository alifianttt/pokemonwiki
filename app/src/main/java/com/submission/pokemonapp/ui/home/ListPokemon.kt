package com.submission.pokemonapp.ui.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.submission.pokemonapp.core.data.Resource
import com.submission.pokemonapp.core.domain.uiadapter.PokemonAdapter
import com.submission.pokemonapp.core.utils.setVisible
import com.submission.pokemonapp.core.utils.toasShort
import com.submission.pokemonapp.databinding.FragmentListPokemonBinding
import com.submission.pokemonapp.ui.detail.DetailActivity
import org.koin.android.viewmodel.ext.android.viewModel

class ListPokemon : Fragment(){

    private val pokemonViewModel: PokemonViewModel by viewModel()
    private val pokemonAdapter : PokemonAdapter by lazy {
        PokemonAdapter()
    }
    private var _binding: FragmentListPokemonBinding? = null
    private val binding get() = _binding!!
    private var isLoading = false
    private var offset = 0
    private val limit = 20
    private var searchQuery = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListPokemonBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (activity != null){

            pokemonAdapter.onItemClick = { selectedItem ->
                requireActivity().startActivity(DetailActivity.newIntent(requireContext(), selectedItem.id.toLong()))
            }

            binding.rvPokemon.apply {
                adapter = pokemonAdapter
                layoutManager = GridLayoutManager(requireActivity(), 2)
            }


            loadMore()
        }

        binding.rvPokemon.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(1)){
                    if (searchQuery.isEmpty()) {
                        offset += limit
                        loadMore()
                    }
                }
            }
        })
    }

    private fun loadMore(){
        pokemonViewModel.getLimitPokemon(offset, limit).observe(viewLifecycleOwner){ pokemon ->
            pokemon.let { poke ->
                when(poke){
                    is Resource.OnLoading -> binding.progressView.setVisible(true)
                    is Resource.OnSucces -> {
                        poke.data?.let { list ->
                            pokemonAdapter.setData(list)
                        }
                        binding.progressView.setVisible(false)
                    }
                    is Resource.OnError -> {
                        binding.progressView.setVisible(false)
                    }
                }
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}