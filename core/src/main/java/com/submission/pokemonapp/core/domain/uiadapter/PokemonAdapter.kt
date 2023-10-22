package com.submission.pokemonapp.core.domain.uiadapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.submission.pokemonapp.core.R
import com.submission.pokemonapp.core.databinding.ItemPokemonBinding
import com.submission.pokemonapp.core.domain.model.Pokemon
import com.submission.pokemonapp.core.utils.Constant
import com.submission.pokemonapp.core.utils.capitalizeFirstLetter
import com.submission.pokemonapp.core.utils.extractColorFromUrl
import com.submission.pokemonapp.core.utils.extractNumberFromUrl

class PokemonAdapter(): RecyclerView.Adapter<PokemonAdapter.PokemonViewHolder>() {

    private var listPokemon = ArrayList<Pokemon>()
    var onItemClick: ((Pokemon) -> Unit)? = null
    private var originalListPokemon = ArrayList<Pokemon>()
    private var filteredListPokemon = ArrayList<Pokemon>()

    private val _diffutil = object :  DiffUtil.ItemCallback<Pokemon>() {
        override fun areItemsTheSame(oldItem: Pokemon, newItem: Pokemon): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: Pokemon, newItem: Pokemon): Boolean {
            return oldItem == newItem
        }
    }


    private val asyncDiffer = AsyncListDiffer(this, _diffutil)

    private val data get() = asyncDiffer.currentList

    private val originalData = mutableListOf<Pokemon>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonViewHolder =
        PokemonViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_pokemon, parent, false))

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: PokemonViewHolder, position: Int) {

        holder.bind(data[position])
    }

    inner class PokemonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = ItemPokemonBinding.bind(itemView)
        fun bind(pokemon: Pokemon){
            val imageUrl = Constant.URL_IMAGE + "${extractNumberFromUrl(pokemon.url).toString().padStart(3, '0')}.png"

            with(binding){
                Glide.with(itemView.context)
                    .load(imageUrl)
                    .into(imagePokemon)
                extractColorFromUrl(itemView.context, imageUrl){ color ->
                    if (color != null)
                        container.setCardBackgroundColor(color)
                }
                pokemonName.text = capitalizeFirstLetter(pokemon.name)
            }
        }

        init {
            binding.root.setOnClickListener {
                onItemClick?.invoke(data[adapterPosition])
            }
        }
    }

    fun setData(newListPokemon: List<Pokemon>){
        val oldList = data.toMutableList()
        oldList.addAll(newListPokemon)
        originalData.addAll(oldList)
        asyncDiffer.submitList(oldList)


    }

    fun filteredPokemon(query: String){
        val filtered = if (query.isEmpty()){
            originalData.toList()
        } else {
            originalData.filter { pokemon ->
                pokemon.name.contains(query, ignoreCase = true)
            }
        }
        Log.d("searched", "size ${filtered.size}")
        asyncDiffer.submitList(filtered)
    }


}