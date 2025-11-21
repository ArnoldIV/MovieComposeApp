package com.taras.pet.movieappcompose.ui.screens.content

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.taras.pet.movieappcompose.domain.model.Movie
import com.taras.pet.movieappcompose.ui.components.PosterImage

@Composable
fun EnhancedMovieItem(
    movie: Movie,
    onClick: () -> Unit,
    favorite: Boolean
) {

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 8.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Poster with rounded corners and shadow
            Card(
                modifier = Modifier.size(width = 90.dp, height = 135.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box {
                    PosterImage(
                        url = movie.posterUrl,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Gradient overlay for better rating visibility
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.3f)
                                    )
                                )
                            )
                    )
                    if (favorite) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFE91E63).copy(alpha = 0.9f)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                contentDescription = "Favorite",
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(16.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(135.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = movie.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = movie.overview,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rating chip
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when {
                            movie.rating >= 8.0 -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                            movie.rating >= 6.0 -> Color(0xFFFFC107).copy(alpha = 0.15f)
                            else -> Color(0xFFF44336).copy(alpha = 0.15f)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Rating",
                                modifier = Modifier.size(14.dp),
                                tint = when {
                                    movie.rating >= 8.0 -> Color(0xFF4CAF50)
                                    movie.rating >= 6.0 -> Color(0xFFFFC107)
                                    else -> Color(0xFFF44336)
                                }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format("%.1f", movie.rating),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = when {
                                    movie.rating >= 8.0 -> Color(0xFF4CAF50)
                                    movie.rating >= 6.0 -> Color(0xFFFFC107)
                                    else -> Color(0xFFF44336)
                                }
                            )
                        }
                    }

                    // Release date
                    Text(
                        text = movie.releaseDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
