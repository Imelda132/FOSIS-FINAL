package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FosisItemEntity
import com.example.data.model.UserEntity
import com.example.data.model.WorkflowStatus


@Composable
fun WorkflowVerificationScreen(

    items: List<FosisItemEntity>,

    activeUser: UserEntity?,

    onSelectItem: (FosisItemEntity) -> Unit,

    onSubmitTeknisi: (FosisItemEntity) -> Unit,

    onVerifyLeader: (FosisItemEntity) -> Unit,

    onToggleLockAdmin: (FosisItemEntity) -> Unit,

    onApproveDeptHead:
        (FosisItemEntity, String) -> Unit

){


    var selectedTier by remember {
        mutableIntStateOf(1)
    }



    val tier1Items =
        items.filter {

            it.workflowStatus ==
                    WorkflowStatus.DRAFT
                    ||
                    it.workflowStatus ==
                    WorkflowStatus.SUBMITTED_TEKNISI

        }



    val tier2Items =
        items.filter {

            it.workflowStatus ==
                    WorkflowStatus.SUBMITTED_TEKNISI

        }



    val tier3Items =
        items.filter {

            it.workflowStatus ==
                    WorkflowStatus.VERIFIED_LEADER
                    ||
                    it.workflowStatus ==
                    WorkflowStatus.LOCKED_ADMIN

        }



    val tier4Items =
        items.filter {

            it.workflowStatus ==
                    WorkflowStatus.LOCKED_ADMIN
                    ||
                    it.workflowStatus ==
                    WorkflowStatus.APPROVED_DEPT_HEAD

        }



    Column(

        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp)

    ){



        Card(

            shape =
                RoundedCornerShape(16.dp)

        ){

            Column(

                modifier =
                    Modifier.padding(14.dp)

            ){


                Row(

                    verticalAlignment =
                        Alignment.CenterVertically

                ){


                    Icon(

                        Icons.Default.AccountTree,

                        contentDescription = null

                    )


                    Spacer(
                        Modifier.width(8.dp)
                    )


                    Text(

                        "Alur Kerja & Verifikasi Sistem",

                        fontWeight =
                            FontWeight.Bold

                    )

                }



                Spacer(
                    Modifier.height(6.dp)
                )


                Text(

                    "Role : ${activeUser?.role?.label}",

                    fontSize =
                        12.sp

                )


            }

        }



        Spacer(
            Modifier.height(14.dp)
        )
        //id="qkz8hz"
        // ===============================
        // TIER SELECTOR
        // ===============================


        SingleChoiceSegmentedButtonRow(

            modifier =
                Modifier.fillMaxWidth()

        ){



            SegmentedButton(

                selected =
                    selectedTier == 1,

                onClick = {
                    selectedTier = 1
                },

                shape =
                    SegmentedButtonDefaults
                        .itemShape(
                            index = 0,
                            count = 4
                        )

            ){

                Text(
                    "1. Teknisi (${tier1Items.size})",
                    fontSize = 10.sp
                )

            }




            SegmentedButton(

                selected =
                    selectedTier == 2,

                onClick = {
                    selectedTier = 2
                },

                shape =
                    SegmentedButtonDefaults
                        .itemShape(
                            index = 1,
                            count = 4
                        )

            ){

                Text(
                    "2. IC (${tier2Items.size})",
                    fontSize = 10.sp
                )

            }





            SegmentedButton(

                selected =
                    selectedTier == 3,

                onClick = {
                    selectedTier = 3
                },

                shape =
                    SegmentedButtonDefaults
                        .itemShape(
                            index = 2,
                            count = 4
                        )

            ){

                Text(
                    "3. Admin (${tier3Items.size})",
                    fontSize = 10.sp
                )

            }





            SegmentedButton(

                selected =
                    selectedTier == 4,

                onClick = {
                    selectedTier = 4
                },

                shape =
                    SegmentedButtonDefaults
                        .itemShape(
                            index = 3,
                            count = 4
                        )

            ){

                Text(
                    "4. Dept Head (${tier4Items.size})",
                    fontSize = 10.sp
                )

            }


        }



        Spacer(
            Modifier.height(14.dp)
        )



        val currentList =

            when(selectedTier){

                1 -> tier1Items

                2 -> tier2Items

                3 -> tier3Items

                else -> tier4Items

            }





        if(currentList.isEmpty()){


            Box(

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),

                contentAlignment =
                    Alignment.Center

            ){

                Text(

                    "Tidak ada tiket pada tahap ini",

                    fontSize =
                        12.sp

                )

            }



        }else{



            LazyColumn(

                modifier =
                    Modifier.weight(1f),

                verticalArrangement =
                    Arrangement.spacedBy(10.dp),

                contentPadding =
                    PaddingValues(
                        bottom = 80.dp
                    )

            ){


                items(currentList){ item ->



                    WorkflowItemCard(

                        item = item,

                        activeUser = activeUser,

                        selectedTier =
                            selectedTier,


                        onClick = {

                            onSelectItem(item)

                        },


                        onSubmitTeknisi = {

                            onSubmitTeknisi(item)

                        },


                        onVerifyLeader = {

                            onVerifyLeader(item)

                        },


                        onToggleLockAdmin = {

                            onToggleLockAdmin(item)

                        },


                        onApproveDeptHead = {

                            onApproveDeptHead(
                                item,
                                "Disetujui dalam review alur."
                            )

                        }

                    )

                }

            }

        }


    }


}
@Composable
private fun WorkflowItemCard(

    item: FosisItemEntity,

    activeUser: UserEntity?,

    selectedTier: Int,

    onClick: () -> Unit,

    onSubmitTeknisi: () -> Unit,

    onVerifyLeader: () -> Unit,

    onToggleLockAdmin: () -> Unit,

    onApproveDeptHead: () -> Unit

){



    Card(

        modifier =
            Modifier
                .fillMaxWidth()
                .clickable {
                    onClick()
                },


        shape =
            RoundedCornerShape(14.dp),


        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )

    ){



        Column(

            modifier =
                Modifier.padding(14.dp)

        ){



            Row(

                modifier =
                    Modifier.fillMaxWidth(),


                horizontalArrangement =
                    Arrangement.SpaceBetween,


                verticalAlignment =
                    Alignment.CenterVertically

            ){



                Text(

                    text =
                        item.ticketNo,


                    fontSize =
                        12.sp,


                    fontWeight =
                        FontWeight.Bold,


                    color =
                        MaterialTheme.colorScheme.primary

                )




                Surface(

                    shape =
                        RoundedCornerShape(10.dp),


                    color =

                        if(item.isLocked)

                            Color(0xFFDCFCE7)

                        else

                            Color(0xFFEFF6FF)

                ){


                    Text(

                        text =
                            item.workflowStatus.label,


                        fontSize =
                            10.sp,


                        fontWeight =
                            FontWeight.Bold,


                        modifier =
                            Modifier.padding(
                                horizontal = 8.dp,
                                vertical = 3.dp
                            )

                    )


                }


            }




            Spacer(
                Modifier.height(5.dp)
            )




            Text(

                text =
                    item.title,


                fontSize =
                    13.sp,


                fontWeight =
                    FontWeight.SemiBold

            )




            Spacer(
                Modifier.height(5.dp)
            )




            Text(

                text =
                    "📍 ${item.siteLocationName} • PIC : ${item.engineerName}",


                fontSize =
                    11.sp

            )





            Spacer(
                Modifier.height(10.dp)
            )





            Row(

                modifier =
                    Modifier.fillMaxWidth(),


                horizontalArrangement =
                    Arrangement.End,


                verticalAlignment =
                    Alignment.CenterVertically

            ){



                when(selectedTier){



                    // =====================
                    // TEKNISI
                    // =====================

                    1 -> {


                        if(
                            item.workflowStatus ==
                            WorkflowStatus.DRAFT
                        ){


                            Button(

                                onClick =
                                    onSubmitTeknisi,


                                contentPadding =
                                    PaddingValues(
                                        horizontal = 12.dp,
                                        vertical = 5.dp
                                    )

                            ){


                                Icon(

                                    Icons.Default.Send,

                                    contentDescription = null,

                                    modifier =
                                        Modifier.size(14.dp)

                                )


                                Spacer(
                                    Modifier.width(4.dp)
                                )


                                Text(
                                    "Submit",
                                    fontSize = 11.sp
                                )

                            }

                        }


                    }




                    // =====================
                    // IC
                    // =====================

                    2 -> {


                        Button(

                            onClick =
                                onVerifyLeader,


                            colors =
                                ButtonDefaults.buttonColors(

                                    containerColor =
                                        Color(0xFF10B981)

                                )

                        ){



                            Icon(

                                Icons.Default.Check,

                                contentDescription = null

                            )


                            Spacer(
                                Modifier.width(4.dp)
                            )


                            Text(
                                "Verifikasi"
                            )


                        }


                    }





                    // =====================
                    // ADMIN
                    // =====================

                    3 -> {


                        Button(

                            onClick =
                                onToggleLockAdmin,


                            colors =
                                ButtonDefaults.buttonColors(

                                    containerColor =

                                        if(item.isLocked)

                                            Color(0xFFD97706)

                                        else

                                            Color(0xFF0F4C81)

                                )

                        ){



                            Icon(

                                imageVector =

                                    if(item.isLocked)

                                        Icons.Default.LockOpen

                                    else

                                        Icons.Default.Lock,


                                contentDescription = null

                            )


                            Spacer(
                                Modifier.width(4.dp)
                            )



                            Text(

                                if(item.isLocked)

                                    "Buka Lock"

                                else

                                    "Lock Data",


                                fontSize = 11.sp

                            )


                        }


                    }





                    // =====================
                    // DEPT HEAD VIEW ONLY
                    // =====================

                    4 -> {



                        Button(

                            onClick = {

                                onClick()

                            },


                            colors =
                                ButtonDefaults.buttonColors(

                                    containerColor =
                                        Color(0xFF2563EB)

                                )

                        ){



                            Icon(

                                Icons.Default.Visibility,

                                contentDescription = null

                            )



                            Spacer(
                                Modifier.width(4.dp)
                            )


                            Text(

                                "View",

                                fontSize = 11.sp

                            )


                        }


                    }


                }


            }


        }


    }


}